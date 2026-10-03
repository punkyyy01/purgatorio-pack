package com.purgatorio.core.menu;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaEvents;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.forge.ForgeGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** Menu minimo de Purgatorio: Alma, forja y Diario. No es el menu final. */
public final class MainMenu {
	private MainMenu() {
	}

	public static SimpleGui open(ServerPlayer player) {
		SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
		gui.setTitle(Component.literal("Purgatorio"));

		int centis = PurgatorioCore.alma().getCentis(player);
		double bonus = (AlmaRules.damageMultiplier(centis) - 1.0) * 100.0;
		gui.setSlot(11, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
			.setName(Component.literal("Tu Alma: " + AlmaEvents.format(centis) + " / 100").withStyle(ChatFormatting.GREEN))
			.addLoreLine(Component.literal(String.format("Bonus de daño: +%.1f%% (máximo +10%%)", bonus)).withStyle(ChatFormatting.GRAY))
			.addLoreLine(Component.literal("Al morir pierdes ~30% de tu Alma.").withStyle(ChatFormatting.RED))
			.addLoreLine(Component.literal("Gastarla en la forja mejora tu equipo para siempre.").withStyle(ChatFormatting.DARK_GRAY)));

		gui.setSlot(13, new GuiElementBuilder(Items.ANVIL)
			.setName(Component.literal("Forja").withStyle(ChatFormatting.GOLD))
			.addLoreLine(Component.literal("Mejora el objeto que tienes en la mano").withStyle(ChatFormatting.GRAY))
			.addLoreLine(Component.literal("con materiales y Alma.").withStyle(ChatFormatting.GRAY))
			.setCallback((index, type, action, gui2) -> ForgeGui.open(player, () -> open(player))));

		GuiElementBuilder diary = new GuiElementBuilder(Items.WRITABLE_BOOK)
			.setName(Component.literal("Diario").withStyle(ChatFormatting.AQUA));
		List<Component> found = discoveries(player);
		if (found.isEmpty()) {
			diary.addLoreLine(Component.literal("Todavía no has descubierto nada.").withStyle(ChatFormatting.GRAY));
		} else {
			diary.addLoreLine(Component.literal("Lo que has descubierto:").withStyle(ChatFormatting.GRAY));
			found.forEach(diary::addLoreLine);
		}
		gui.setSlot(15, diary);
		gui.open();
		return gui;
	}

	/** Solo lo YA descubierto: el Diario no revela lugares que el jugador no ha visto. */
	public static List<Component> discoveries(ServerPlayer player) {
		List<Component> out = new ArrayList<>();
		for (AdvancementHolder holder : player.level().getServer().getAdvancements().getAllAdvancements()) {
			String path = holder.id().getPath();
			if (!holder.id().getNamespace().equals(PurgatorioCore.NAMESPACE) || !path.startsWith("diario/") || path.equals("diario/raiz")) {
				continue;
			}
			if (player.getAdvancements().getOrStartProgress(holder).isDone()) {
				DisplayInfo display = holder.value().display().orElse(null);
				if (display != null) {
					out.add(Component.literal("• ").append(display.title()).withStyle(ChatFormatting.WHITE));
				}
			}
		}
		return out;
	}
}
