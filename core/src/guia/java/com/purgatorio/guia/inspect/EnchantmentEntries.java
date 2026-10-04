package com.purgatorio.guia.inspect;

import com.purgatorio.guia.gui.Ui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Lee los encantamientos de un objeto (puestos o guardados en un libro) y crea una entrada por cada uno. Si el objeto
 * no tiene ninguno, lista los que admite y puede obtener el jugador.
 */
public final class EnchantmentEntries {
	private static final int WRAP = 40;
	private static final int MAX_EXCLUSIVE_SHOWN = 5;

	/** @param enchanted true si el objeto tiene encantamientos; false si la lista es la de los que admite */
	public record Result(boolean enchanted, List<GuiElementBuilder> entries) {
	}

	private EnchantmentEntries() {
	}

	public static Result of(ServerPlayer player, ItemStack stack) {
		List<GuiElementBuilder> out = new ArrayList<>();
		List<Object[]> present = new ArrayList<>();
		for (var component : List.of(DataComponents.ENCHANTMENTS, DataComponents.STORED_ENCHANTMENTS)) {
			ItemEnchantments ench = stack.getOrDefault(component, ItemEnchantments.EMPTY);
			for (var e : ench.entrySet()) {
				present.add(new Object[] {e.getKey(), e.getIntValue()});
			}
		}
		if (!present.isEmpty()) {
			for (Object[] p : present) {
				@SuppressWarnings("unchecked") Holder<Enchantment> holder = (Holder<Enchantment>) p[0];
				out.add(entry(holder, (Integer) p[1]));
			}
			return new Result(true, out);
		}
		var registry = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		registry.listElements()
			.filter(h -> h.value().isSupportedItem(stack) && obtainable(h))
			.sorted(Comparator.comparing((Holder.Reference<Enchantment> h) -> !h.key().identifier().getNamespace().equals("minecraft"))
				.thenComparing(h -> h.key().identifier().toString()))
			.forEach(h -> out.add(entry(h, null)));
		return new Result(false, out);
	}

	/** Ficha de catalogo: la entrada sin nivel mas una linea por nivel con su efecto (donde la descripcion lo define). */
	public static GuiElementBuilder catalogEntry(Holder<Enchantment> holder) {
		GuiElementBuilder b = entry(holder, null);
		Descriptions.Entry d = Descriptions.get(idOf(holder));
		if (d != null) {
			boolean any = false;
			for (int lvl = 1; lvl <= holder.value().getMaxLevel(); lvl++) {
				String line = d.effectAt(lvl);
				if (line == null) {
					continue;
				}
				if (!any) {
					b.addLoreLine(Ui.blank());
					b.addLoreLine(Ui.text("Por nivel:", ChatFormatting.AQUA));
					any = true;
				}
				b.addLoreLine(Ui.text(Text.roman(lvl) + ": " + line, ChatFormatting.YELLOW));
			}
		}
		return b;
	}

	/** Mismo criterio que tools/inventario-guia.py para "de jugador": alguna via normal de conseguirlo. */
	public static boolean obtainable(Holder<Enchantment> h) {
		return h.is(EnchantmentTags.IN_ENCHANTING_TABLE) || h.is(EnchantmentTags.TRADEABLE) || h.is(EnchantmentTags.TREASURE)
			|| h.is(EnchantmentTags.NON_TREASURE) || h.is(EnchantmentTags.ON_RANDOM_LOOT) || h.is(EnchantmentTags.CURSE);
	}

	public static String idOf(Holder<Enchantment> holder) {
		return holder.unwrapKey().map(k -> k.identifier().toString()).orElse("?");
	}

	/** Una entrada: libro encantado con nombre, descripcion y datos. {@code level} null = solo catalogo (sin nivel). */
	public static GuiElementBuilder entry(Holder<Enchantment> holder, Integer level) {
		Enchantment enchantment = holder.value();
		boolean curse = holder.is(EnchantmentTags.CURSE);
		ChatFormatting color = curse ? ChatFormatting.RED : ChatFormatting.GOLD;
		MutableComponent name = (level != null ? Enchantment.getFullname(holder, level) : enchantment.description())
			.copy().withStyle(color).withStyle(s -> s.withBold(true).withItalic(false));
		GuiElementBuilder b = new GuiElementBuilder(Items.ENCHANTED_BOOK).glow().hideDefaultTooltip().setName(name);

		String id = idOf(holder);
		Descriptions.Entry d = Descriptions.get(id);
		if (d == null) {
			b.addLoreLine(Ui.text("Sin descripción todavía.", ChatFormatting.DARK_GRAY));
			b.addLoreLine(Ui.text(id, ChatFormatting.DARK_GRAY));
		} else {
			Text.wrap(d.desc(), WRAP).forEach(l -> b.addLoreLine(Ui.text(l, ChatFormatting.GRAY)));
		}
		b.addLoreLine(Ui.blank());
		if (level != null) {
			String effect = d == null ? null : d.effectAt(level);
			if (effect != null) {
				b.addLoreLine(Ui.text(effect, ChatFormatting.YELLOW));
			}
			b.addLoreLine(Ui.text("Nivel " + level + " de " + enchantment.getMaxLevel(), ChatFormatting.AQUA));
			if (level > enchantment.getMaxLevel()) {
				b.addLoreLine(Ui.text("Por encima del máximo normal", ChatFormatting.LIGHT_PURPLE));
			}
		} else {
			b.addLoreLine(Ui.text("Nivel máximo: " + enchantment.getMaxLevel(), ChatFormatting.AQUA));
		}
		if (curse) {
			b.addLoreLine(Ui.text("Maldición: es un efecto negativo.", ChatFormatting.RED));
		}
		MutableComponent incompatible = Component.empty();
		int shown = 0;
		int total = 0;
		for (Holder<Enchantment> other : enchantment.exclusiveSet()) {
			if (other.equals(holder) || idOf(other).equals(id)) {
				continue;
			}
			total++;
			if (shown < MAX_EXCLUSIVE_SHOWN) {
				if (shown++ > 0) {
					incompatible.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
				}
				incompatible.append(other.value().description().copy().withStyle(ChatFormatting.GRAY));
			}
		}
		if (total > 0) {
			if (total > shown) {
				incompatible.append(Component.literal(" y " + (total - shown) + " más").withStyle(ChatFormatting.DARK_GRAY));
			}
			b.addLoreLine(Ui.text("No combina con:", ChatFormatting.RED));
			b.addLoreLine(incompatible.withStyle(s -> s.withItalic(false)));
		}
		return b;
	}
}
