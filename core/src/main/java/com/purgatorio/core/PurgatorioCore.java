package com.purgatorio.core;

import com.purgatorio.core.alma.AlmaEvents;
import com.purgatorio.core.alma.AlmaService;
import com.purgatorio.core.alma.AttachmentAlmaStorage;
import com.purgatorio.core.alma.XpBarAlmaDisplay;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PurgatorioCore implements ModInitializer {
	public static final String MOD_ID = "purgatorio_core";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static AlmaService alma;

	/** Servicio de Alma. Disponible desde que el mod se inicializa. */
	public static AlmaService alma() {
		return alma;
	}

	@Override
	public void onInitialize() {
		alma = new AlmaService(new AttachmentAlmaStorage(), new XpBarAlmaDisplay());
		AlmaEvents.register(alma);
		LOGGER.info("Purgatorio Core cargado");
	}
}
