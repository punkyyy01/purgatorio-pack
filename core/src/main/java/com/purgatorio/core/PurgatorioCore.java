package com.purgatorio.core;

import com.purgatorio.core.alma.AlmaEvents;
import com.purgatorio.core.alma.AlmaService;
import com.purgatorio.core.alma.AttachmentAlmaStorage;
import com.purgatorio.core.alma.XpBarAlmaDisplay;
import com.purgatorio.core.forge.ForgeRecipeLoader;
import com.purgatorio.core.forge.ForgeService;
import com.purgatorio.core.item.ColmilloTrait;
import com.purgatorio.core.item.PurgatorioItems;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PurgatorioCore implements ModInitializer {
	public static final String MOD_ID = "purgatorio_core";
	/** Espacio de nombres del contenido del juego (items, logros, tablas, recetas de forja). */
	public static final String NAMESPACE = "purgatorio";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static AlmaService alma;
	private static ForgeRecipeLoader forgeRecipes;
	private static ForgeService forgeService;

	/** Servicio de Alma. Disponible desde que el mod se inicializa. */
	public static AlmaService alma() {
		return alma;
	}

	public static ForgeRecipeLoader forgeRecipes() {
		return forgeRecipes;
	}

	public static ForgeService forgeService() {
		return forgeService;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(NAMESPACE, path);
	}

	@Override
	public void onInitialize() {
		alma = new AlmaService(new AttachmentAlmaStorage(), new XpBarAlmaDisplay());
		AlmaEvents.register(alma);

		PurgatorioItems.init();
		ColmilloTrait.register();
		forgeRecipes = ForgeRecipeLoader.register();
		forgeService = new ForgeService(alma);

		// Los modelos/texturas propios van al resource pack automatico de Polymer.
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		LOGGER.info("Purgatorio Core cargado");
	}
}
