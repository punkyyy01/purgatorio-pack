package com.purgatorio.guia.gui;

import com.purgatorio.guia.inspect.Topics;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Guia del servidor: lista de temas (mochilas, tumbas, piedras de viaje...); cada uno abre su pantalla. */
public final class ServerGuideGui extends PagedGui {
	private final Runnable toHub;

	public ServerGuideGui(ServerPlayer player, Runnable onBack) {
		super(player, "Guía del servidor", onBack);
		this.toHub = onBack;
	}

	/** Icono de un tema: el objeto indicado si existe; si no, un libro. */
	static Item iconOf(String id) {
		return BuiltInRegistries.ITEM.get(Identifier.parse(id)).map(h -> h.value()).orElse(Items.BOOK);
	}

	@Override
	protected List<GuiElementBuilder> entries() {
		List<GuiElementBuilder> out = new ArrayList<>();
		Runnable back = () -> new ServerGuideGui(player, toHub).open();
		for (Topics.Topic topic : Topics.available()) {
			GuiElementBuilder b = new GuiElementBuilder(new ItemStack(iconOf(topic.icono()))).hideDefaultTooltip()
				.setName(Ui.title(topic.titulo()));
			com.purgatorio.guia.inspect.Text.wrap(topic.resumen(), 40).forEach(l -> b.addLoreLine(Ui.text(l, ChatFormatting.GRAY)));
			b.addLoreLine(Ui.blank());
			b.addLoreLine(Ui.text("▶ Clic para abrir", ChatFormatting.YELLOW));
			b.setCallback((index, type, action, g) -> new TopicGui(player, topic, back).open());
			out.add(b);
		}
		return out;
	}
}
