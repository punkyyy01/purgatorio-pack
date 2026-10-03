package com.purgatorio.core;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PurgatorioCore implements ModInitializer {
	public static final String MOD_ID = "purgatorio_core";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Purgatorio Core cargado");
	}
}
