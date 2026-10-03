package com.purgatorio.core.forge;

import com.purgatorio.core.PurgatorioCore;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Carga las recetas de forja del datapack (recargable con /reload). */
public final class ForgeRecipeLoader extends SimpleJsonResourceReloadListener<ForgeRecipe> {
	private volatile Map<Identifier, ForgeRecipe> byItem = Map.of();

	private ForgeRecipeLoader() {
		super(ForgeRecipe.CODEC, FileToIdConverter.json("forja"));
	}

	public static ForgeRecipeLoader register() {
		ForgeRecipeLoader loader = new ForgeRecipeLoader();
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(PurgatorioCore.id("forja"), loader);
		return loader;
	}

	@Override
	protected void apply(Map<Identifier, ForgeRecipe> recipes, ResourceManager manager, ProfilerFiller profiler) {
		Map<Identifier, ForgeRecipe> map = new HashMap<>();
		for (ForgeRecipe recipe : recipes.values()) {
			map.put(recipe.item(), recipe);
		}
		this.byItem = Map.copyOf(map);
		PurgatorioCore.LOGGER.info("Recetas de forja cargadas: {}", this.byItem.keySet());
	}

	/** Receta para un item, o null si no es mejorable. */
	public ForgeRecipe forItem(Identifier item) {
		return byItem.get(item);
	}

	public int size() {
		return byItem.size();
	}
}
