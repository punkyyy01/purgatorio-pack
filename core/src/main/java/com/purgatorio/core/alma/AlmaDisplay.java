package com.purgatorio.core.alma;

import net.minecraft.server.level.ServerPlayer;

/** Como se le muestra el Alma al jugador. */
public interface AlmaDisplay {
	void show(ServerPlayer player, int centis);
}
