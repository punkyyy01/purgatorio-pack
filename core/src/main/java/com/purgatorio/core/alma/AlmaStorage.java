package com.purgatorio.core.alma;

import net.minecraft.server.level.ServerPlayer;

/** Donde vive el Alma de cada jugador (en centesimas). Separado del servicio para poder cambiar la persistencia. */
public interface AlmaStorage {
	int get(ServerPlayer player);

	void set(ServerPlayer player, int centis);
}
