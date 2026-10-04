package com.purgatorio.guia.inspect;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.purgatorio.guia.gui.Ui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;

/**
 * Lo que pasa al consumir un objeto y NO es "aplicar efectos" (eso lo explica {@link EffectEntries}): la leche y la miel
 * quitan efectos, la fruta de chorus teletransporta, y los de Farmer's Delight curan, apagan el fuego o quitan un efecto
 * al azar. Todo se lee de los datos del objeto; los efectos de mods se leen por su codificacion JSON (sin depender del mod).
 */
public final class ConsumeEntries {
	private static final int WRAP = 40;

	private ConsumeEntries() {
	}

	/** Lista vacia si consumirlo no hace nada de esto. */
	public static List<GuiElementBuilder> of(ItemStack stack) {
		Consumable consumable = stack.get(DataComponents.CONSUMABLE);
		if (consumable == null) {
			return List.of();
		}
		List<Component> lines = new ArrayList<>();
		for (ConsumeEffect effect : consumable.onConsumeEffects()) {
			Component line = describe(effect);
			if (line != null) {
				lines.add(line);
			}
		}
		if (lines.isEmpty()) {
			return List.of();
		}
		GuiElementBuilder b = new GuiElementBuilder(Items.HONEY_BOTTLE).hideDefaultTooltip()
			.setName(Ui.text("Al consumirlo", ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
		lines.forEach(b::addLoreLine);
		return List.of(b);
	}

	/** null para lo que no hay que explicar (aplicar efectos, sonidos...). */
	static Component describe(ConsumeEffect effect) {
		if (effect instanceof ClearAllStatusEffectsConsumeEffect) {
			return Ui.text("Elimina todos tus efectos activos.", ChatFormatting.YELLOW);
		}
		if (effect instanceof RemoveStatusEffectsConsumeEffect remove) {
			MutableComponent names = Component.empty().withStyle(s -> s.withItalic(false));
			boolean first = true;
			for (var holder : remove.effects()) {
				if (!first) {
					names.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
				}
				names.append(holder.value().getDisplayName().copy().withStyle(ChatFormatting.YELLOW));
				first = false;
			}
			return Component.empty().append(Ui.text("Elimina el efecto: ", ChatFormatting.YELLOW)).append(names);
		}
		if (effect instanceof TeleportRandomlyConsumeEffect teleport) {
			return Ui.text("Te teletransporta al azar (hasta " + Text.num(teleport.diameter() / 2.0) + " bloques).", ChatFormatting.YELLOW);
		}
		// Efectos de otros mods (Farmer's Delight...): se leen por su JSON, sin depender de sus clases.
		JsonObject json = ConsumeEffect.CODEC.encodeStart(JsonOps.INSTANCE, effect).result()
			.filter(e -> e.isJsonObject()).map(e -> e.getAsJsonObject()).orElse(null);
		if (json == null || !json.has("type")) {
			return null;
		}
		return switch (json.get("type").getAsString()) {
			case "farmersdelight:heal" -> {
				double amount = json.has("amount") ? json.get("amount").getAsDouble() : 0;
				double hearts = amount / 2;
				yield Ui.text("Cura " + Text.num(amount) + " puntos de vida (" + Text.num(hearts) + (hearts == 1.0 ? " corazón" : " corazones") + ").", ChatFormatting.YELLOW);
			}
			case "farmersdelight:extinguish" -> Ui.text("Apaga el fuego que te quema.", ChatFormatting.YELLOW);
			case "farmersdelight:remove_random_effects" -> Ui.text(json.has("harmful_only") && json.get("harmful_only").getAsBoolean()
				? "Elimina un efecto negativo al azar." : "Elimina un efecto activo al azar.", ChatFormatting.YELLOW);
			default -> null;
		};
	}
}
