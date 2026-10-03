package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.forge.ForgeService;
import com.purgatorio.core.item.ColmilloDeCeniza;
import com.purgatorio.core.item.PurgatorioItems;
import com.purgatorio.core.item.UpgradeData;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** Forja: mejora permanente pagada con materiales y Alma. */
public final class ForgeSuite implements Suite {
	@Override
	public String name() {
		return "forja";
	}

	static Item shard() {
		return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("purgatorio", "esquirla_de_brasa")).orElse(null);
	}

	/** Dano de ataque que aporta el arma segun su componente de atributos. */
	static double weaponDamage(ItemStack stack) {
		ItemAttributeModifiers mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
		if (mods == null) {
			return Double.NaN;
		}
		for (ItemAttributeModifiers.Entry e : mods.modifiers()) {
			if (e.attribute().is(Attributes.ATTACK_DAMAGE)) {
				return e.modifier().amount();
			}
		}
		return Double.NaN;
	}

	static int count(ServerPlayer p, Item item) {
		Inventory inv = p.getInventory();
		int n = 0;
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (inv.getItem(i).is(item)) {
				n += inv.getItem(i).getCount();
			}
		}
		return n;
	}

	static boolean hasSword(ServerPlayer p) {
		return findSword(p) != null;
	}

	static ItemStack findSword(ServerPlayer p) {
		Inventory inv = p.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (inv.getItem(i).getItem() instanceof ColmilloDeCeniza) {
				return inv.getItem(i);
			}
		}
		return null;
	}

	static void clear(ServerPlayer p) {
		p.getInventory().clearContent();
	}

	static ItemStack giveSword(ServerPlayer p, int level) {
		ItemStack sword = new ItemStack(PurgatorioItems.COLMILLO_DE_CENIZA);
		UpgradeData.set(sword, level);
		PurgatorioItems.COLMILLO_DE_CENIZA.applyLevel(sword, level);
		p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword);
		return p.getMainHandItem();
	}

	@Override
	public void run(Ctx ctx) {
		Ctx.Mock a = ctx.join("forja-test-a");
		Ctx.Mock b = ctx.join("forja-test-b");
		ServerPlayer pa = a.player();
		ServerPlayer pb = b.player();
		ForgeService forge = PurgatorioCore.forgeService();
		var alma = PurgatorioCore.alma();

		// ---- datos cargados desde el datapack ----
		var recipe = PurgatorioCore.forgeRecipes().forItem(Identifier.fromNamespaceAndPath("purgatorio", "colmillo_de_ceniza"));
		ctx.check(recipe != null, "la receta del Colmillo se cargo desde el datapack");
		if (recipe != null) {
			ctx.eq(3, recipe.maxLevel(), "la receta tiene 3 niveles");
			ctx.eq(10, recipe.levels().get(0).alma(), "nivel I cuesta 10 de Alma");
		}
		Item shardItem = shard();
		ctx.check(shardItem != null, "el item de Filament purgatorio:esquirla_de_brasa esta registrado");

		// ---- un item no mejorable ----
		clear(pa);
		ItemStack plain = new ItemStack(Items.IRON_SWORD);
		pa.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, plain);
		ctx.check(!forge.preview(pa, plain).upgradable(), "una espada de hierro vanilla no es mejorable");
		ctx.check(!forge.tryUpgrade(pa, plain).ok(), "mejorar un item no mejorable falla");

		// ---- fallo: sin Alma suficiente ----
		clear(pa);
		alma.set(pa, AlmaRules.fromPoints(5));
		ItemStack sword = giveSword(pa, 0);
		pa.getInventory().add(new ItemStack(Items.IRON_INGOT, 4));
		ctx.check(!forge.tryUpgrade(pa, sword).ok(), "con 5 de Alma no se puede pagar el nivel I (10)");
		ctx.eq(0, UpgradeData.get(sword), "un intento fallido no sube el nivel");
		ctx.eq(AlmaRules.fromPoints(5), alma.getCentis(pa), "un intento fallido no gasta Alma");
		ctx.eq(4, count(pa, Items.IRON_INGOT), "un intento fallido no consume materiales");

		// ---- fallo: faltan materiales ----
		clear(pa);
		alma.set(pa, AlmaRules.fromPoints(50));
		sword = giveSword(pa, 0);
		pa.getInventory().add(new ItemStack(Items.IRON_INGOT, 3));
		ctx.check(!forge.tryUpgrade(pa, sword).ok(), "con 3 lingotes (se piden 4) falla");
		ctx.eq(AlmaRules.fromPoints(50), alma.getCentis(pa), "faltan materiales: no se gasta Alma");
		ctx.eq(3, count(pa, Items.IRON_INGOT), "faltan materiales: no se consumen los que habia");

		// ---- exito nivel I: cobra materiales + Alma, permanente, baja el bonus ----
		pa.getInventory().add(new ItemStack(Items.IRON_INGOT, 1));
		ForgeService.Preview preview = forge.preview(pa, sword);
		ctx.check(preview.canUpgrade(), "con 50 de Alma y 4 lingotes el nivel I es posible");
		ctx.eq(AlmaRules.fromPoints(10), preview.almaCostCentis(), "el coste de Alma es visible en la vista previa (10)");
		ctx.near(5.0, weaponDamage(sword), 1e-9, "antes de mejorar el dano base es 5");
		double multBefore = alma.damageMultiplier(pa);
		ForgeService.Outcome outcome = forge.tryUpgrade(pa, sword);
		ctx.check(outcome.ok(), "mejora I aplicada: " + outcome.message().getString());
		ctx.eq(1, UpgradeData.get(sword), "el nivel del item sube a 1");
		ctx.near(6.5, weaponDamage(sword), 1e-9, "el dano del arma sube a 6,5 (5 + 1,5)");
		ctx.eq(AlmaRules.fromPoints(40), alma.getCentis(pa), "gasta 10 de Alma (50 -> 40)");
		ctx.eq(0, count(pa, Items.IRON_INGOT), "consume los 4 lingotes");
		ctx.check(alma.damageMultiplier(pa) < multBefore, "gastar Alma reduce el bonus de dano temporal");
		ctx.near(1.04, alma.damageMultiplier(pa), 1e-9, "con 40 de Alma el bonus es +4%");
		ctx.eq(0, alma.getCentis(pb), "B (Alma 0) no se ve afectado por la forja de A");

		// ---- nivel II y III con materiales de Filament ----
		if (shardItem != null) {
			alma.set(pa, AlmaRules.MAX);
			pa.getInventory().add(new ItemStack(shardItem, 2));
			ctx.check(forge.tryUpgrade(pa, sword).ok(), "nivel II con 2 esquirlas + 20 de Alma");
			ctx.eq(AlmaRules.fromPoints(80), alma.getCentis(pa), "100 - 20 = 80");
			ctx.eq(0, count(pa, shardItem), "consume las 2 esquirlas");
			pa.getInventory().add(new ItemStack(shardItem, 4));
			ctx.check(!forge.tryUpgrade(pa, sword).ok(), "nivel III sin diamante falla");
			ctx.eq(4, count(pa, shardItem), "el fallo no consume las esquirlas");
			pa.getInventory().add(new ItemStack(Items.DIAMOND, 1));
			ctx.check(forge.tryUpgrade(pa, sword).ok(), "nivel III con 4 esquirlas + 1 diamante + 30 de Alma");
			ctx.eq(AlmaRules.fromPoints(50), alma.getCentis(pa), "80 - 30 = 50");
			ctx.eq(3, UpgradeData.get(sword), "nivel 3");
			ctx.near(5.0 + 1.5 * 3, weaponDamage(sword), 1e-9, "dano nivel 3 = 9,5");
			// ---- maximo ----
			pa.getInventory().add(new ItemStack(Items.IRON_INGOT, 64));
			ctx.check(!forge.tryUpgrade(pa, sword).ok() && forge.preview(pa, sword).maxed(), "al maximo no se puede mejorar mas");
			ctx.eq(AlmaRules.fromPoints(50), alma.getCentis(pa), "al maximo no se cobra Alma");

			// ---- permanente: sobrevive a salir y entrar ----
			clear(pa);
			pa.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, sword.copy());
			ctx.leave(a);
			Ctx.Mock a2 = ctx.join("forja-test-a");
			ItemStack back = a2.player().getMainHandItem();
			ctx.check(back.getItem() instanceof ColmilloDeCeniza, "el Colmillo sigue en la mano tras reconectar");
			ctx.eq(3, UpgradeData.get(back), "la mejora (3) es permanente tras reconectar");
			ctx.near(9.5, weaponDamage(back), 1e-9, "el dano mejorado persiste");
			ctx.eq(AlmaRules.fromPoints(50), alma.getCentis(a2.player()), "y el Alma restante (50) tambien");
		}

		// ---- el gasto de Alma de un jugador no toca al otro; B tiene su propia forja ----
		clear(pb);
		alma.set(pb, AlmaRules.fromPoints(12));
		ItemStack swordB = giveSword(pb, 0);
		pb.getInventory().add(new ItemStack(Items.IRON_INGOT, 4));
		ctx.check(forge.tryUpgrade(pb, swordB).ok(), "B mejora su propia espada con SU Alma");
		ctx.eq(AlmaRules.fromPoints(2), alma.getCentis(pb), "B: 12 - 10 = 2");
		if (shardItem != null) {
			ctx.eq(AlmaRules.fromPoints(50), alma.getCentis(ctx.server.getPlayerList().getPlayerByName("forja-test-a")), "A conserva su Alma (50) mientras B forja");
		}
	}
}
