package com.purgatorio.guia.gui;

import com.purgatorio.guia.inspect.Descriptions;
import com.purgatorio.guia.inspect.EnchantmentEntries;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

/** Catalogo de todos los encantamientos descritos, filtrable por tipo de objeto, origen y maldiciones. */
public final class EnchantmentCatalogGui extends PagedGui {
	/** Tipo de objeto: un encantamiento pasa el filtro si admite alguno de los objetos de muestra. */
	record Category(String label, List<ItemStack> samples) {
	}

	private static final Map<String, String> ORIGINS = Map.ofEntries(
		Map.entry("minecraft", "Vanilla"), Map.entry("enchantments", "More Enchants"), Map.entry("enchantsplus", "Enchants Plus"),
		Map.entry("nova_structures", "Dungeons & Taverns"), Map.entry("dke", "Dragonkind Evolved"), Map.entry("dqc.bows", "Advanced Archery"),
		Map.entry("warft", "Fortress of War"), Map.entry("farmersdelight", "Farmer's Delight"), Map.entry("incendium", "Incendium"),
		Map.entry("serverbackpacks", "Server Backpacks"));

	/** Maldiciones: null = todos, true = solo maldiciones, false = sin maldiciones. */
	private static final List<Option<Boolean>> CURSES = List.of(
		new Option<>("Todos", null), new Option<>("Solo maldiciones", true), new Option<>("Sin maldiciones", false));

	private final List<Holder.Reference<Enchantment>> all;
	private final List<Category> categories;
	private final List<String> origins;
	private int category;
	private int origin;
	private int curse;

	public EnchantmentCatalogGui(ServerPlayer player, Runnable onBack) {
		super(player, "Catálogo de encantamientos", onBack);
		this.all = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
			.filter(h -> Descriptions.get(EnchantmentEntries.idOf(h)) != null)
			.sorted(Comparator.comparing((Holder.Reference<Enchantment> h) -> !h.key().identifier().getNamespace().equals("minecraft"))
				.thenComparing(h -> h.key().identifier().toString()))
			.toList();
		this.categories = categories();
		Set<String> namespaces = new LinkedHashSet<>();
		all.forEach(h -> namespaces.add(h.key().identifier().getNamespace()));
		this.origins = new ArrayList<>(namespaces);
	}

	static List<Category> categories() {
		List<Category> out = new ArrayList<>();
		add(out, "Espadas", "minecraft:diamond_sword");
		add(out, "Hachas", "minecraft:diamond_axe");
		add(out, "Herramientas (pico, pala, azada)", "minecraft:diamond_pickaxe", "minecraft:diamond_shovel", "minecraft:diamond_hoe");
		add(out, "Arcos", "minecraft:bow");
		add(out, "Ballestas", "minecraft:crossbow");
		add(out, "Tridentes", "minecraft:trident");
		add(out, "Mazas", "minecraft:mace");
		add(out, "Lanzas", "minecraft:iron_spear");
		add(out, "Cascos", "minecraft:diamond_helmet");
		add(out, "Petos", "minecraft:diamond_chestplate");
		add(out, "Pantalones", "minecraft:diamond_leggings");
		add(out, "Botas", "minecraft:diamond_boots");
		add(out, "Élitros", "minecraft:elytra");
		add(out, "Escudos", "minecraft:shield");
		add(out, "Cañas de pescar", "minecraft:fishing_rod");
		return out;
	}

	private static void add(List<Category> out, String label, String... ids) {
		List<ItemStack> samples = new ArrayList<>();
		for (String id : ids) {
			BuiltInRegistries.ITEM.get(Identifier.parse(id)).ifPresent(h -> samples.add(new ItemStack(h)));
		}
		if (!samples.isEmpty()) {
			out.add(new Category(label, samples));
		}
	}

	static String originName(String namespace) {
		return ORIGINS.getOrDefault(namespace, namespace);
	}

	@Override
	protected List<GuiElementBuilder> entries() {
		List<GuiElementBuilder> out = new ArrayList<>();
		for (Holder.Reference<Enchantment> h : all) {
			if (category > 0 && categories.get(category - 1).samples().stream().noneMatch(s -> h.value().isSupportedItem(s))) {
				continue;
			}
			if (origin > 0 && !h.key().identifier().getNamespace().equals(origins.get(origin - 1))) {
				continue;
			}
			Boolean wanted = CURSES.get(curse).value();
			if (wanted != null && wanted != h.is(EnchantmentTags.CURSE)) {
				continue;
			}
			out.add(EnchantmentEntries.catalogEntry(h));
		}
		return out;
	}

	@Override
	protected void addFilters() {
		List<String> categoryLabels = new ArrayList<>(List.of("Todos"));
		categories.forEach(c -> categoryLabels.add(c.label()));
		cycle(FILTER_1, Items.IRON_SWORD, "Tipo de objeto", categoryLabels, category, i -> category = i, s -> s);
		List<String> originLabels = new ArrayList<>(List.of("Todos"));
		origins.forEach(o -> originLabels.add(originName(o)));
		cycle(FILTER_2, Items.COMPASS, "Origen", originLabels, origin, i -> origin = i, s -> s);
		cycle(FILTER_3, Items.WITHER_SKELETON_SKULL, "Maldiciones", CURSES, curse, i -> curse = i, Option::label);
	}
}
