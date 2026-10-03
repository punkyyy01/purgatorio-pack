package com.purgatorio.core.forge;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.alma.AlmaService;
import com.purgatorio.core.item.UpgradeData;
import com.purgatorio.core.item.Upgradeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Forja de prueba: mejora un item gastando materiales y Alma. Valida todo antes de tocar nada, asi
 * que una mejora o se aplica completa o no cambia nada.
 */
public final class ForgeService {
	/** Estado de un material requerido para el siguiente nivel. */
	public record MaterialStatus(ForgeRecipe.Material material, Item item, int have) {
		public boolean enough() {
			return item != null && have >= material.count();
		}
	}

	/** Todo lo que el jugador necesita ver antes de mejorar. {@code next == null} si no es mejorable o ya esta al maximo. */
	public record Preview(ForgeRecipe recipe, int currentLevel, ForgeRecipe.Level next, List<MaterialStatus> materials,
						  int almaCostCentis, int almaHaveCentis) {
		public boolean upgradable() {
			return recipe != null;
		}

		public boolean maxed() {
			return recipe != null && next == null;
		}

		public boolean enoughAlma() {
			return AlmaRules.canSpend(almaHaveCentis, almaCostCentis);
		}

		public boolean canUpgrade() {
			return next != null && enoughAlma() && materials.stream().allMatch(MaterialStatus::enough);
		}
	}

	public record Outcome(boolean ok, Component message) {
	}

	private final AlmaService alma;

	public ForgeService(AlmaService alma) {
		this.alma = alma;
	}

	public Preview preview(ServerPlayer player, ItemStack stack) {
		if (stack.isEmpty() || !(stack.getItem() instanceof Upgradeable)) {
			return new Preview(null, 0, null, List.of(), 0, alma.getCentis(player));
		}
		ForgeRecipe recipe = PurgatorioCore.forgeRecipes().forItem(BuiltInRegistries.ITEM.getKey(stack.getItem()));
		if (recipe == null) {
			return new Preview(null, 0, null, List.of(), 0, alma.getCentis(player));
		}
		int level = UpgradeData.get(stack);
		if (level >= recipe.maxLevel()) {
			return new Preview(recipe, level, null, List.of(), 0, alma.getCentis(player));
		}
		ForgeRecipe.Level next = recipe.levels().get(level);
		List<MaterialStatus> materials = new ArrayList<>();
		for (ForgeRecipe.Material material : next.materials()) {
			Optional<Item> item = BuiltInRegistries.ITEM.getOptional(material.item());
			materials.add(new MaterialStatus(material, item.orElse(null), item.map(i -> count(player, i, stack)).orElse(0)));
		}
		return new Preview(recipe, level, next, materials, AlmaRules.fromPoints(next.alma()), alma.getCentis(player));
	}

	/** Intenta subir un nivel el item {@code stack} (el de la mano del jugador). */
	public Outcome tryUpgrade(ServerPlayer player, ItemStack stack) {
		Preview preview = preview(player, stack);
		if (!preview.upgradable()) {
			return new Outcome(false, Component.literal("Este objeto no se puede mejorar en la forja."));
		}
		if (preview.maxed()) {
			return new Outcome(false, Component.literal("Este objeto ya está al máximo."));
		}
		if (!preview.enoughAlma()) {
			return new Outcome(false, Component.literal("No tienes Alma suficiente: necesitas " + preview.next().alma()
				+ " y tienes " + com.purgatorio.core.alma.AlmaEvents.format(preview.almaHaveCentis()) + "."));
		}
		for (MaterialStatus status : preview.materials()) {
			if (!status.enough()) {
				return new Outcome(false, Component.literal("Te faltan materiales: " + status.material().item() + " x" + status.material().count() + "."));
			}
		}
		// Todo validado: ahora si se cobra y se aplica.
		if (!alma.spend(player, preview.almaCostCentis())) {
			return new Outcome(false, Component.literal("No tienes Alma suficiente."));
		}
		for (MaterialStatus status : preview.materials()) {
			remove(player, status.item(), status.material().count(), stack);
		}
		int newLevel = preview.currentLevel() + 1;
		UpgradeData.set(stack, newLevel);
		((Upgradeable) stack.getItem()).applyLevel(stack, newLevel);
		return new Outcome(true, Component.literal("Mejora aplicada: nivel " + newLevel + ". Gastaste " + preview.next().alma() + " de Alma."));
	}

	private static int count(ServerPlayer player, Item item, ItemStack ignore) {
		Inventory inventory = player.getInventory();
		int total = 0;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack s = inventory.getItem(i);
			if (s != ignore && s.is(item)) {
				total += s.getCount();
			}
		}
		return total;
	}

	private static void remove(ServerPlayer player, Item item, int amount, ItemStack ignore) {
		Inventory inventory = player.getInventory();
		int left = amount;
		for (int i = 0; i < inventory.getContainerSize() && left > 0; i++) {
			ItemStack s = inventory.getItem(i);
			if (s != ignore && s.is(item)) {
				int take = Math.min(left, s.getCount());
				s.shrink(take);
				left -= take;
			}
		}
	}
}
