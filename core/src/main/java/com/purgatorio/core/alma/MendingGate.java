package com.purgatorio.core.alma;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;

/**
 * Mending (y cualquier encantamiento con el efecto {@code repair_with_xp}) repara al recoger una orbe de XP: necesita
 * que la orbe EXISTA, no que haya XP. Como el XP vanilla ya no existe, las orbes solo se crean cuando hay alguien cerca
 * con un objeto danado que las pueda usar. Asi no hay orbes inutiles (ni lag en granjas de XP) y el XP nunca da Alma.
 */
public final class MendingGate {
	/** Radio (bloques) en el que un jugador con un objeto reparable "atrae" la orbe (las orbes siguen al mas cercano a 8). */
	public static final double RADIUS = 16.0;

	private MendingGate() {
	}

	/** El jugador lleva un objeto danado con un efecto de reparacion por XP (Mending). */
	public static boolean needsRepair(ServerPlayer player) {
		return EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, player, ItemStack::isDamaged).isPresent();
	}

	/** Hay algun jugador cerca de {@code pos} al que una orbe de XP le serviria para reparar. */
	public static boolean anyoneNearNeedsRepair(ServerLevel level, Vec3 pos) {
		double max = RADIUS * RADIUS;
		for (ServerPlayer player : level.players()) {
			if (!player.isSpectator() && player.position().distanceToSqr(pos) <= max && needsRepair(player)) {
				return true;
			}
		}
		return false;
	}
}
