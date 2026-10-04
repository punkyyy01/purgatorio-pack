package com.purgatorio.guia.inspect;

import com.purgatorio.guia.gui.Ui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Objetos especiales descritos a mano (objetos.json): mochilas, llaves, materiales de mods... Una sola entrada "Qué es"
 * con lo que es, como se usa y como se consigue. Las descripciones se escriben con lo observado en el juego (datos,
 * configuracion del servidor o el objeto usado de verdad en las pruebas), no de memoria.
 */
public final class SpecialEntries {
	private static final int WRAP = 40;

	private SpecialEntries() {
	}

	public static List<GuiElementBuilder> of(ItemStack stack) {
		Descriptions.ItemInfo info = Descriptions.item(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
		if (info == null) {
			return List.of();
		}
		GuiElementBuilder b = new GuiElementBuilder(Items.BOOK).hideDefaultTooltip()
			.setName(Ui.text("Qué es", ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD));
		Text.wrap(info.desc(), WRAP).forEach(l -> b.addLoreLine(Ui.text(l, ChatFormatting.GRAY)));
		section(b, "Cómo se usa", info.uso());
		section(b, "Cómo se consigue", info.consigue());
		return List.of(b);
	}

	private static void section(GuiElementBuilder b, String title, List<String> lines) {
		if (lines.isEmpty()) {
			return;
		}
		b.addLoreLine(Ui.blank());
		b.addLoreLine(Ui.text(title + ":", ChatFormatting.AQUA));
		for (String line : lines) {
			boolean first = true;
			for (String wrapped : Text.wrap(line, WRAP - 2)) {
				b.addLoreLine(Ui.text((first ? "• " : "  ") + wrapped, ChatFormatting.YELLOW));
				first = false;
			}
		}
	}
}
