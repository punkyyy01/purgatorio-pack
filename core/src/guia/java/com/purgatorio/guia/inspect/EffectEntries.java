package com.purgatorio.guia.inspect;

import com.purgatorio.guia.gui.Ui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

/**
 * Efectos que da un objeto: pociones (y flechas con efecto), estofado sospechoso, botella ominosa y cualquier
 * comida o consumible que aplique efectos. Una entrada por efecto, con descripcion escrita a mano, el efecto en ese
 * nivel y las cifras de atributos que el propio juego calcula (no se escriben a mano).
 */
public final class EffectEntries {
	private static final int WRAP = 40;

	private EffectEntries() {
	}

	/** Lista vacia si el objeto no da ningun efecto. */
	public static List<GuiElementBuilder> of(ServerPlayer player, ItemStack stack) {
		List<GuiElementBuilder> out = new ArrayList<>();

		PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
		if (potion != null) {
			String note = formNote(stack);
			boolean any = false;
			for (MobEffectInstance instance : potion.getAllEffects()) {
				out.add(entry(instance, null, note));
				any = true;
			}
			if (!any) {
				potion.potion().ifPresent(base -> out.add(basePotionEntry(base)));
			}
		}
		SuspiciousStewEffects stew = stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
		if (stew != null) {
			for (SuspiciousStewEffects.Entry e : stew.effects()) {
				out.add(entry(e.createEffectInstance(), null, null));
			}
		}
		OminousBottleAmplifier ominous = stack.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
		if (ominous != null) {
			out.add(entry(new MobEffectInstance(MobEffects.BAD_OMEN, OminousBottleAmplifier.EFFECT_DURATION, ominous.value()), null, null));
		}
		Consumable consumable = stack.get(DataComponents.CONSUMABLE);
		if (consumable != null) {
			for (ConsumeEffect effect : consumable.onConsumeEffects()) {
				if (effect instanceof ApplyStatusEffectsConsumeEffect apply) {
					for (MobEffectInstance instance : apply.effects()) {
						out.add(entry(instance, apply.probability(), null));
					}
				}
			}
		}
		return out;
	}

	/** El tooltip de flechas y pociones persistentes muestra la duracion ya reducida; aqui se avisa de ello. */
	private static String formNote(ItemStack stack) {
		if (stack.is(Items.TIPPED_ARROW)) {
			return "En una flecha, la duración real es 1/8 de la indicada.";
		}
		if (stack.is(Items.LINGERING_POTION)) {
			return "En la nube persistente, cada exposición dura 1/4 de la indicada.";
		}
		if (stack.is(Items.SPLASH_POTION)) {
			return "Arrojadiza: cuanto más lejos del impacto, menos dura.";
		}
		return null;
	}

	public static String idOf(Holder<MobEffect> holder) {
		return holder.unwrapKey().map(k -> k.identifier().toString()).orElse("?");
	}

	/** Una entrada: pocion del color del efecto con nombre, descripcion, efecto en ese nivel y duracion. */
	public static GuiElementBuilder entry(MobEffectInstance instance, Float probability, String note) {
		Holder<MobEffect> holder = instance.getEffect();
		MobEffect effect = holder.value();
		int amplifier = instance.getAmplifier();
		int level = amplifier + 1;

		ItemStack icon = new ItemStack(Items.POTION);
		icon.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(effect.getColor()), List.of(), Optional.empty()));
		MutableComponent name = PotionContents.getPotionDescription(holder, amplifier)
			.withStyle(effect.getCategory().getTooltipFormatting()).withStyle(s -> s.withBold(true).withItalic(false));
		GuiElementBuilder b = new GuiElementBuilder(icon).hideDefaultTooltip().setName(name);

		String id = idOf(holder);
		Descriptions.Entry d = Descriptions.effect(id);
		if (d == null) {
			b.addLoreLine(Ui.text("Sin descripción todavía.", ChatFormatting.DARK_GRAY));
			b.addLoreLine(Ui.text(id, ChatFormatting.DARK_GRAY));
		} else {
			Text.wrap(d.desc(), WRAP).forEach(l -> b.addLoreLine(Ui.text(l, ChatFormatting.GRAY)));
		}
		b.addLoreLine(Ui.blank());

		String line = d == null ? null : d.effectAt(level);
		if (line != null) {
			b.addLoreLine(Ui.text(line, ChatFormatting.YELLOW));
		}
		if (d == null || d.atributos()) {
			effect.createModifiers(amplifier, (attribute, modifier) -> b.addLoreLine(attributeLine(attribute, modifier)));
		}

		b.addLoreLine(Ui.text("Nivel " + level, ChatFormatting.AQUA));
		if (effect.isInstantaneous()) {
			b.addLoreLine(Ui.text("Efecto instantáneo", ChatFormatting.AQUA));
		} else if (instance.isInfiniteDuration()) {
			b.addLoreLine(Ui.text("Duración: infinita", ChatFormatting.AQUA));
		} else {
			b.addLoreLine(Ui.text("Duración: " + Text.duration(instance.getDuration()), ChatFormatting.AQUA));
		}
		if (probability != null && probability < 1.0F) {
			b.addLoreLine(Ui.text("Probabilidad: " + Text.num(probability * 100.0) + " %", ChatFormatting.LIGHT_PURPLE));
		}
		if (note != null) {
			b.addLoreLine(Ui.text(note, ChatFormatting.DARK_GRAY));
		}
		return b;
	}

	/** "Velocidad de movimiento: +20 %": el nombre lo traduce el cliente; la cifra sale del propio juego. */
	static Component attributeLine(Holder<Attribute> attribute, AttributeModifier modifier) {
		boolean percent = modifier.operation() != AttributeModifier.Operation.ADD_VALUE;
		double amount = modifier.amount();
		String value = (amount >= 0 ? "+" : "-") + Text.num(Math.abs(percent ? amount * 100.0 : amount)) + (percent ? " %" : "");
		return Component.empty()
			.append(Component.translatable(attribute.value().getDescriptionId()).withStyle(ChatFormatting.YELLOW))
			.append(Component.literal(": " + value).withStyle(ChatFormatting.YELLOW))
			.withStyle(s -> s.withItalic(false));
	}

	/** Pocion sin efectos (agua, rara, vulgar, densa...): explica para que sirve. */
	static GuiElementBuilder basePotionEntry(Holder<Potion> base) {
		ItemStack icon = new ItemStack(Items.POTION);
		icon.set(DataComponents.POTION_CONTENTS, new PotionContents(base));
		PotionContents contents = new PotionContents(base);
		GuiElementBuilder b = new GuiElementBuilder(icon).hideDefaultTooltip()
			.setName(contents.getName("item.minecraft.potion.effect.").copy().withStyle(ChatFormatting.GRAY)
				.withStyle(s -> s.withBold(true).withItalic(false)));
		String id = base.unwrapKey().map(k -> k.identifier().toString()).orElse("?");
		Descriptions.Entry d = Descriptions.effect(Descriptions.POTION_PREFIX + id);
		if (d == null) {
			b.addLoreLine(Ui.text("Sin descripción todavía.", ChatFormatting.DARK_GRAY));
			b.addLoreLine(Ui.text(id, ChatFormatting.DARK_GRAY));
		} else {
			Text.wrap(d.desc(), WRAP).forEach(l -> b.addLoreLine(Ui.text(l, ChatFormatting.GRAY)));
		}
		return b;
	}
}
