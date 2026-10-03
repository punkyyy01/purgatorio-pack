package com.purgatorio.core.alma;

import com.mojang.serialization.Codec;
import com.purgatorio.core.PurgatorioCore;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Persistencia por jugador con Fabric Data Attachments: se guarda junto a los datos normales del
 * jugador (individual por definicion) y se copia al reaparecer.
 */
public final class AttachmentAlmaStorage implements AlmaStorage {
	public static final AttachmentType<Integer> ALMA = AttachmentRegistry.create(
		Identifier.fromNamespaceAndPath(PurgatorioCore.MOD_ID, "alma"),
		builder -> builder.persistent(Codec.INT).copyOnDeath().initializer(() -> 0)
	);

	@Override
	public int get(ServerPlayer player) {
		return AlmaRules.clamp(player.getAttachedOrElse(ALMA, 0));
	}

	@Override
	public void set(ServerPlayer player, int centis) {
		player.setAttached(ALMA, AlmaRules.clamp(centis));
	}
}
