package com.purgatorio.guia.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** Menu principal de la guia: es lo que abre el libro y /guia. */
public final class HubGui {
	static final int INSPECTOR_SLOT = 10;
	static final int ENCHANTMENTS_SLOT = 12;
	static final int EFFECTS_SLOT = 14;
	static final int GUIDE_SLOT = 16;

	private HubGui() {
	}

	public static SimpleGui open(ServerPlayer player) {
		SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
		gui.setTitle(Ui.title("Guía del Purgatorio"));
		for (int i = 0; i < 27; i++) {
			gui.setSlot(i, Ui.filler());
		}
		Runnable back = () -> open(player);
		gui.setSlot(INSPECTOR_SLOT, button(Items.HOPPER, "Inspector de objetos",
			"Elige un objeto de tu inventario y te explica", "qué es, qué encantamientos tiene y qué hace.")
			.setCallback((index, type, action, g) -> InspectorGui.open(player, back)));
		gui.setSlot(ENCHANTMENTS_SLOT, button(Items.ENCHANTED_BOOK, "Catálogo de encantamientos",
			"Todos los encantamientos del servidor, con lo que", "hacen en cada nivel. Filtra por objeto u origen.").glow()
			.setCallback((index, type, action, g) -> new EnchantmentCatalogGui(player, back).open()));
		gui.setSlot(EFFECTS_SLOT, button(Items.POTION, "Efectos y pociones",
			"Todos los efectos del juego y de los mods, y", "todas las pociones con sus efectos y duración.")
			.setCallback((index, type, action, g) -> new EffectCatalogGui(player, back).open()));
		gui.setSlot(GUIDE_SLOT, button(Items.KNOWLEDGE_BOOK, "Guía del servidor",
			"Mochilas, tumbas, piedras de viaje y otras", "cosas que conviene saber.")
			.setCallback((index, type, action, g) -> new ServerGuideGui(player, back).open()));
		gui.setSlot(22, new GuiElementBuilder(Items.BARRIER).setName(Ui.text("Cerrar", ChatFormatting.RED))
			.setCallback((index, type, action, g) -> gui.close()));
		gui.open();
		return gui;
	}

	private static GuiElementBuilder button(net.minecraft.world.item.Item icon, String name, String line1, String line2) {
		return new GuiElementBuilder(icon).hideDefaultTooltip()
			.setName(Ui.title(name))
			.addLoreLine(Ui.text(line1, ChatFormatting.GRAY))
			.addLoreLine(Ui.text(line2, ChatFormatting.GRAY))
			.addLoreLine(Ui.blank())
			.addLoreLine(Ui.text("▶ Clic para abrir", ChatFormatting.YELLOW));
	}
}
