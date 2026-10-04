package com.purgatorio.guia;

import com.purgatorio.guia.book.GuideBookEvents;
import com.purgatorio.guia.gui.InspectorGui;
import com.purgatorio.guia.inspect.Descriptions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Libro-guia del jugador. Mod independiente: no usa nada de purgatorio-core. */
public final class PurgatorioGuia implements ModInitializer {
	public static final String MOD_ID = "purgatorio_guia";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Descriptions.load();
		GuideBookEvents.register();
		// /guia: abre la guia aunque no se tenga el libro a mano.
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
			dispatcher.register(Commands.literal("guia").executes(c -> {
				InspectorGui.open(c.getSource().getPlayerOrException());
				return 1;
			})));
		LOGGER.info("Purgatorio Guia cargada");
	}
}
