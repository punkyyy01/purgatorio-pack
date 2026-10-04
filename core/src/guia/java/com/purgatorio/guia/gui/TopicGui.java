package com.purgatorio.guia.gui;

import com.purgatorio.guia.inspect.SpecialEntries;
import com.purgatorio.guia.inspect.Text;
import com.purgatorio.guia.inspect.Topics;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Un tema de la guia del servidor: sus parrafos y, debajo, los objetos de los que habla con su ficha. */
public final class TopicGui extends PagedGui {
	private static final int WRAP = 40;
	private final Topics.Topic topic;

	public TopicGui(ServerPlayer player, Topics.Topic topic, Runnable onBack) {
		super(player, topic.titulo(), onBack);
		this.topic = topic;
	}

	@Override
	protected List<GuiElementBuilder> entries() {
		List<GuiElementBuilder> out = new ArrayList<>();
		for (Topics.Paragraph p : topic.parrafos()) {
			GuiElementBuilder b = new GuiElementBuilder(Items.PAPER).hideDefaultTooltip()
				.setName(Ui.title(p.titulo()));
			Text.wrap(p.texto(), WRAP).forEach(l -> b.addLoreLine(Ui.text(l, ChatFormatting.GRAY)));
			out.add(b);
		}
		for (String id : topic.objetos()) {
			BuiltInRegistries.ITEM.get(Identifier.parse(id)).ifPresent(holder -> {
				GuiElementBuilder card = SpecialEntries.card(new ItemStack(holder));
				if (card != null) {
					out.add(card);
				}
			});
		}
		return out;
	}
}
