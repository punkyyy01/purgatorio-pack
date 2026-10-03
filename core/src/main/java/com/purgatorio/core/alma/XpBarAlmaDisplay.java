package com.purgatorio.core.alma;

import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Muestra el Alma en la barra de XP vanilla enviando el paquete directamente. El XP real del
 * jugador no se toca: un mixin sustituye ademas los valores en los reenvios de vanilla.
 */
public final class XpBarAlmaDisplay implements AlmaDisplay {
	@Override
	public void show(ServerPlayer player, int centis) {
		if (player.connection == null) {
			return;
		}
		int level = AlmaRules.displayLevel(centis);
		player.connection.send(new ClientboundSetExperiencePacket(AlmaRules.displayProgress(centis), level, level));
	}
}
