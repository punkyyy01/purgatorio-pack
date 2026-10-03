package com.purgatorio.core.feedback;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** Efectos privados para un jugador (sonido, particulas, titulos, texto sobre la barra). Todo viaja solo a ese jugador. */
public final class Fx {
	private Fx() {
	}

	public static void sound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
		sound(player, Holder.direct(sound), player.getX(), player.getY(), player.getZ(), volume, pitch);
	}

	public static void sound(ServerPlayer player, Holder<SoundEvent> sound, double x, double y, double z, float volume, float pitch) {
		if (player.connection != null) {
			player.connection.send(new ClientboundSoundPacket(sound, SoundSource.PLAYERS, x, y, z, volume, pitch, player.getRandom().nextLong()));
		}
	}

	/** Sonido posicional (se oye de lejos si el volumen > 1: alcance ~ 16 x volumen bloques). */
	public static void soundAt(ServerPlayer player, SoundEvent sound, double x, double y, double z, float volume, float pitch) {
		sound(player, Holder.direct(sound), x, y, z, volume, pitch);
	}

	public static void particles(ServerPlayer player, ParticleOptions particle, double x, double y, double z, int count,
								 double dx, double dy, double dz, double speed) {
		player.level().sendParticles(player, particle, false, false, x, y, z, count, dx, dy, dz, speed);
	}

	/** Particulas visibles desde muy lejos (para pistas de exploracion). */
	public static void particlesFar(ServerPlayer player, ParticleOptions particle, double x, double y, double z, int count,
									double dx, double dy, double dz, double speed) {
		player.level().sendParticles(player, particle, true, true, x, y, z, count, dx, dy, dz, speed);
	}

	public static void title(ServerPlayer player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
		if (player.connection == null) {
			return;
		}
		player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
		player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
		player.connection.send(new ClientboundSetTitleTextPacket(title));
	}

	public static void actionbar(ServerPlayer player, Component text) {
		player.sendSystemMessage(text, true);
	}
}
