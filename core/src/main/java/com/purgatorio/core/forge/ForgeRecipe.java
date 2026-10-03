package com.purgatorio.core.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.resources.Identifier;

/**
 * Receta de mejora de un item (datapack: {@code data/<ns>/forja/<item>.json}). Cada nivel cuesta
 * Alma (en puntos enteros) mas materiales. Las mejoras son permanentes.
 */
public record ForgeRecipe(Identifier item, List<Level> levels) {
	public record Material(Identifier item, int count) {
		public static final Codec<Material> CODEC = RecordCodecBuilder.create(i -> i.group(
			Identifier.CODEC.fieldOf("item").forGetter(Material::item),
			Codec.intRange(1, 99).fieldOf("count").forGetter(Material::count)
		).apply(i, Material::new));
	}

	public record Level(int alma, List<Material> materials) {
		public static final Codec<Level> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.intRange(0, 100).fieldOf("alma").forGetter(Level::alma),
			Material.CODEC.listOf().fieldOf("materials").forGetter(Level::materials)
		).apply(i, Level::new));
	}

	public static final Codec<ForgeRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
		Identifier.CODEC.fieldOf("item").forGetter(ForgeRecipe::item),
		Level.CODEC.listOf().fieldOf("levels").forGetter(ForgeRecipe::levels)
	).apply(i, ForgeRecipe::new));

	public int maxLevel() {
		return levels.size();
	}
}
