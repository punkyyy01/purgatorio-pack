package com.purgatorio.core.feedback;

import com.purgatorio.core.alma.AlmaEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

/** La voz de Purgatorio cuando el Alma cambia: ganar, perder y gastar. Nunca un mensaje tecnico a secas. */
public final class AlmaFeedback {
	private AlmaFeedback() {
	}

	public static void onGain(ServerPlayer player, int gainedCentis, AlmaReason reason) {
		if (reason == AlmaReason.SILENT || gainedCentis <= 0) {
			return;
		}
		String amount = AlmaEvents.format(gainedCentis);
		String line = switch (reason) {
			case DESCUBRIMIENTO -> "El lugar te reconoce.  +" + amount + " de Alma";
			case DERROTA -> "Algo de lo que fue se queda contigo.  +" + amount + " de Alma";
			default -> "Tu Alma crece.  +" + amount + " de Alma";
		};
		Fx.actionbar(player, Component.literal(line).withStyle(ChatFormatting.GREEN));
		Fx.sound(player, SoundEvents.AMETHYST_BLOCK_CHIME, 0.9F, 0.7F + Math.min(0.5F, gainedCentis / 4000.0F));
		Fx.particles(player, ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 6 + Math.min(14, gainedCentis / 100), 0.4, 0.6, 0.4, 0.03);
	}

	/** Tras reaparecer: el momento de la perdida. */
	public static void onDeathLoss(ServerPlayer player, int lostCentis, int remainingCentis) {
		if (lostCentis <= 0) {
			return;
		}
		Fx.title(player,
			Component.literal("Tu Alma se derrama").withStyle(ChatFormatting.DARK_GREEN),
			Component.literal("−" + AlmaEvents.format(lostCentis) + "  ·  te quedan " + AlmaEvents.format(remainingCentis)).withStyle(ChatFormatting.GRAY),
			8, 55, 25);
		Fx.sound(player, SoundEvents.SOUL_ESCAPE.value(), 1.0F, 0.8F);
		Fx.particles(player, ParticleTypes.SOUL, player.getX(), player.getY() + 1.0, player.getZ(), 18, 0.5, 0.8, 0.5, 0.04);
	}

	/** Al forjar: el Alma se funde en el metal. */
	public static void onSpend(ServerPlayer player, int spentCentis) {
		Fx.actionbar(player, Component.literal("La mejora arde en el metal.  −" + AlmaEvents.format(spentCentis) + " de Alma").withStyle(ChatFormatting.GOLD));
		Fx.sound(player, SoundEvents.ANVIL_USE, 0.7F, 1.1F);
		Fx.sound(player, SoundEvents.FIRECHARGE_USE, 0.8F, 0.7F);
		Fx.particles(player, ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 14, 0.3, 0.5, 0.3, 0.03);
	}
}
