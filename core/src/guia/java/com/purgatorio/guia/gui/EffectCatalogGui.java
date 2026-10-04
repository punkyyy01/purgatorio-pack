package com.purgatorio.guia.gui;

import com.purgatorio.guia.inspect.EffectEntries;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;

/** Catalogo de efectos (con lo que hacen por nivel) y de pociones (con sus efectos y duracion). */
public final class EffectCatalogGui extends PagedGui {
	/** Modo: false = efectos, true = pociones. */
	private static final List<Option<Boolean>> MODES = List.of(new Option<>("Efectos", false), new Option<>("Pociones", true));
	private static final List<Option<MobEffectCategory>> CATEGORIES = List.of(new Option<>("Todos", null),
		new Option<>("Beneficiosos", MobEffectCategory.BENEFICIAL), new Option<>("Perjudiciales", MobEffectCategory.HARMFUL),
		new Option<>("Neutros", MobEffectCategory.NEUTRAL));
	/** Pociones: null = todas, true = con efectos, false = sin efectos (bases). */
	private static final List<Option<Boolean>> POTION_KINDS = List.of(new Option<>("Todas", null),
		new Option<>("Con efectos", true), new Option<>("Sin efectos (bases)", false));

	private int mode;
	private int category;
	private int potionKind;

	public EffectCatalogGui(ServerPlayer player, Runnable onBack) {
		super(player, "Efectos y pociones", onBack);
	}

	private static <T> Comparator<Holder.Reference<T>> byId() {
		return Comparator.comparing((Holder.Reference<T> h) -> !h.key().identifier().getNamespace().equals("minecraft"))
			.thenComparing(h -> h.key().identifier().toString());
	}

	@Override
	protected List<GuiElementBuilder> entries() {
		List<GuiElementBuilder> out = new ArrayList<>();
		if (!MODES.get(mode).value()) {
			MobEffectCategory wanted = CATEGORIES.get(category).value();
			BuiltInRegistries.MOB_EFFECT.listElements().sorted(EffectCatalogGui.<MobEffect>byId()).forEach(h -> {
				if (wanted == null || h.value().getCategory() == wanted) {
					out.add(EffectEntries.catalogEntry(h));
				}
			});
		} else {
			Boolean wanted = POTION_KINDS.get(potionKind).value();
			BuiltInRegistries.POTION.listElements().sorted(EffectCatalogGui.<Potion>byId()).forEach(h -> {
				if (wanted == null || wanted == !h.value().getEffects().isEmpty()) {
					out.add(EffectEntries.potionCatalogEntry(h));
				}
			});
		}
		return out;
	}

	@Override
	protected void addFilters() {
		cycle(FILTER_1, Items.POTION, "Mostrar", MODES, mode, i -> mode = i, Option::label);
		if (!MODES.get(mode).value()) {
			cycle(FILTER_2, Items.GLOWSTONE_DUST, "Tipo de efecto", CATEGORIES, category, i -> category = i, Option::label);
		} else {
			cycle(FILTER_2, Items.BREWING_STAND, "Pociones", POTION_KINDS, potionKind, i -> potionKind = i, Option::label);
		}
	}
}
