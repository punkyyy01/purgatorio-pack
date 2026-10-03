package com.purgatorio.core.feedback;

import com.purgatorio.core.PurgatorioCore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Pistas de exploracion SIN marcadores: cerca de La Ruina de Ceniza cae ceniza en el aire y se oye una campana lejana
 * que viene de su direccion. No dice "aqui hay algo": hace que quieras ir a ver de donde sale.
 *
 * <p>Se consulta cada 2 s, solo en chunks ya cargados cerca del jugador (nunca genera ni busca estructuras).
 */
public final class PlaceAmbience {
	static final int SCAN_CHUNKS = 5;                 // 80 bloques a la redonda
	static final double ASH_RANGE = 64.0;
	static final double BELL_RANGE = 80.0;
	static final int BELL_EVERY_TICKS = 200;          // ~10 s de media entre campanadas

	private static final Map<UUID, Long> LAST_BELL = new HashMap<>();

	private PlaceAmbience() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 40 != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (!player.isSpectator()) {
					tickPlayer(server, player);
				}
			}
		});
		ServerPlayerEvents.LEAVE.register(p -> LAST_BELL.remove(p.getUUID()));
	}

	/**
	 * Centro de la Ruina de Ceniza mas cercana entre los chunks YA CARGADOS cerca del jugador, o null.
	 * Importante: NO usa {@code StructureManager.startsForStructure}, que genera los chunks que no existen; aqui solo se leen
	 * los que el servidor ya tiene en memoria, asi que la consulta es barata y nunca provoca generacion de mundo.
	 */
	public static BlockPos nearestRuin(ServerLevel level, ServerPlayer player) {
		Structure ruin = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(PurgatorioCore.id("ruina_de_ceniza"));
		if (ruin == null) {
			return null;
		}
		ChunkPos pc = ChunkPos.containing(player.blockPosition());
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (int dx = -SCAN_CHUNKS; dx <= SCAN_CHUNKS; dx++) {
			for (int dz = -SCAN_CHUNKS; dz <= SCAN_CHUNKS; dz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(pc.x() + dx, pc.z() + dz);
				if (chunk == null) {
					continue;
				}
				StructureStart start = chunk.getStartForStructure(ruin);
				if (start == null || !start.isValid()) {
					continue;
				}
				BlockPos center = start.getBoundingBox().getCenter();
				double d = Math.hypot(center.getX() + 0.5 - player.getX(), center.getZ() + 0.5 - player.getZ());
				if (d < bestDist) {
					bestDist = d;
					best = center;
				}
			}
		}
		return best;
	}

	public static void tickPlayer(MinecraftServer server, ServerPlayer player) {
		ServerLevel level = player.level();
		BlockPos ruin = nearestRuin(level, player);
		if (ruin == null) {
			return;
		}
		double dist = Math.hypot(ruin.getX() + 0.5 - player.getX(), ruin.getZ() + 0.5 - player.getZ());
		if (dist <= ASH_RANGE) {
			// Ceniza que cae: mas densa cuanto mas cerca.
			int count = dist < 24 ? 10 : 5;
			Fx.particles(player, ParticleTypes.ASH, player.getX(), player.getY() + 2.0, player.getZ(), count, 7.0, 3.0, 7.0, 0.0);
		}
		if (dist <= BELL_RANGE) {
			long now = server.getTickCount();
			Long last = LAST_BELL.get(player.getUUID());
			if ((last == null || now - last >= BELL_EVERY_TICKS) && player.getRandom().nextInt(3) == 0) {
				LAST_BELL.put(player.getUUID(), now);
				// Sonido POSICIONAL desde la ruina (volumen > 1 = se oye a mas distancia): la direccion es la pista.
				Fx.soundAt(player, SoundEvents.BELL_RESONATE, ruin.getX() + 0.5, ruin.getY() + 8.0, ruin.getZ() + 0.5, 5.0F, 0.5F);
			}
		}
	}
}
