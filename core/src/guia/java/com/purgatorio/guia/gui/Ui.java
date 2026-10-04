package com.purgatorio.guia.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

/** Piezas de presentacion de la guia (solo estetica). Copia minima: este mod no depende del nucleo. */
public final class Ui {
	private Ui() {
	}

	/** Texto de tooltip: con color y sin la cursiva por defecto del lore. */
	public static MutableComponent text(String text, ChatFormatting color) {
		return Component.literal(text).withStyle(color).withStyle(style -> style.withItalic(false));
	}

	public static MutableComponent title(String text) {
		return Component.literal(text).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD).withStyle(style -> style.withItalic(false));
	}

	public static Component blank() {
		return Component.literal(" ");
	}

	/** Panel de relleno sin tooltip: da marco oscuro a la pantalla. */
	public static GuiElementBuilder filler() {
		return new GuiElementBuilder(Items.STAINED_GLASS_PANE.black()).setName(Component.literal(" ")).hideTooltip();
	}
}
