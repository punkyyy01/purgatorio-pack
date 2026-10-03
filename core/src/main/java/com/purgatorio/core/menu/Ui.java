package com.purgatorio.core.menu;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

/** Piezas de presentacion compartidas por los menus (solo estetica, sin logica de juego). */
public final class Ui {
	private Ui() {
	}

	public static MutableComponent text(String text, ChatFormatting color) {
		return Component.literal(text).withStyle(color);
	}

	public static MutableComponent title(String text) {
		return Component.literal(text).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
	}

	/** Linea en blanco para separar bloques de un tooltip. */
	public static Component blank() {
		return Component.literal(" ");
	}

	/** Panel de relleno sin tooltip: da un marco oscuro al menu. */
	public static GuiElementBuilder filler() {
		return new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(" ")).hideTooltip();
	}

	public static void fillAll(SimpleGui gui, int slots) {
		for (int i = 0; i < slots; i++) {
			gui.setSlot(i, filler());
		}
	}

	/** Barra de texto: bloques llenos en {@code color} y vacios en gris. */
	public static MutableComponent bar(double fraction, int width, ChatFormatting color) {
		int filled = (int) Math.round(Math.max(0.0, Math.min(1.0, fraction)) * width);
		return Component.empty()
			.append(Component.literal("█".repeat(filled)).withStyle(color))
			.append(Component.literal("░".repeat(width - filled)).withStyle(ChatFormatting.DARK_GRAY));
	}

	/** Numero con coma decimal, sin ceros sobrantes ("6,5", "8"). */
	public static String num(double value) {
		String s = String.format(Locale.ROOT, "%.1f", value);
		if (s.endsWith(".0")) {
			s = s.substring(0, s.length() - 2);
		}
		return s.replace('.', ',');
	}
}
