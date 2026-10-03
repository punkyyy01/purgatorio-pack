package com.purgatorio.core.item;

import com.purgatorio.core.PurgatorioCore;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;

/** Items propios implementados en Java (los que necesitan rasgos con codigo). Los de solo datos van en Filament. */
public final class PurgatorioItems {
	public static final ColmilloDeCeniza COLMILLO_DE_CENIZA = register("colmillo_de_ceniza",
		key -> new ColmilloDeCeniza(new Item.Properties()
			.sword(ToolMaterial.IRON, 3.0F, (float) ColmilloDeCeniza.ATTACK_SPEED)
			.rarity(Rarity.RARE)
			.setId(key)));

	private PurgatorioItems() {
	}

	private static <T extends Item> T register(String path, Function<ResourceKey<Item>, T> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, PurgatorioCore.id(path));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(key));
	}

	/** Fuerza la carga de la clase (y con ella el registro de items). */
	public static void init() {
	}
}
