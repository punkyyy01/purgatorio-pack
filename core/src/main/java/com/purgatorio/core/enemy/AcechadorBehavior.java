package com.purgatorio.core.enemy;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.feedback.AlmaReason;
import com.purgatorio.core.feedback.Diary;
import com.purgatorio.core.feedback.Fx;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * El Acechador de Ceniza: un enemigo cuya "pregunta al jugador" es <i>"¿como me protejo de los
 * ataques por la espalda?"</i>. Cuando tiene como objetivo a un jugador lejano, avisa (humo y
 * sonido) y 1 s despues se teletransporta a su espalda. El aviso (telegraph) es justo.
 */
public final class AcechadorBehavior {
	public static final String TAG = "purgatorio.acechador";
	/** Alma que da matarlo (el botin real es la Esquirla de Brasa). */
	public static final int ALMA_REWARD_CENTIS = 300;

	static final int COOLDOWN_TICKS = 160;
	static final int TELEGRAPH_TICKS = 20;
	static final double MIN_DISTANCE = 4.0;
	static final double MAX_DISTANCE = 24.0;
	static final double BEHIND_OFFSET = 1.8;

	private static final class State {
		final Mob mob;
		int cooldown = 100;
		int telegraph = 0;
		Vec3 destination;

		State(Mob mob) {
			this.mob = mob;
		}
	}

	/** Solo se toca desde el hilo del servidor. */
	private static final Map<UUID, State> STATES = new HashMap<>();

	private AcechadorBehavior() {
	}

	public static void register() {
		// Entidades que se cargan del disco (ya tienen la etiqueta). Las recien creadas se registran con track().
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof Mob mob && mob.entityTags().contains(TAG)) {
				track(mob);
			}
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> STATES.remove(entity.getUUID()));
		ServerTickEvents.END_SERVER_TICK.register(AcechadorBehavior::tick);
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((level, killer, killed, source) -> {
			if (killer instanceof ServerPlayer player && killed.entityTags().contains(TAG)) {
				PurgatorioCore.alma().add(player, ALMA_REWARD_CENTIS, AlmaReason.DERROTA);
				Diary.award(player, "acechador");       // la primera vez: titulo "Ceniza al viento" (funcion del datapack)
				Fx.sound(player, SoundEvents.WITHER_BREAK_BLOCK, 0.5F, 1.4F);
			}
		});
	}

	/** Empieza a seguir a un Acechador (idempotente). */
	public static void track(Mob mob) {
		STATES.computeIfAbsent(mob.getUUID(), id -> {
			EnemySpawner.applyDefinition(mob);
			return new State(mob);
		});
	}

	public static int trackedCount() {
		return STATES.size();
	}

	/** Un tick de comportamiento para todos los Acechadores cargados. Publico para poder probarlo. */
	public static void tick(MinecraftServer server) {
		Iterator<State> it = STATES.values().iterator();
		while (it.hasNext()) {
			State state = it.next();
			Mob mob = state.mob;
			if (mob.isRemoved() || !mob.isAlive()) {
				it.remove();
				continue;
			}
			if (mob.level() instanceof ServerLevel level) {
				tickOne(level, state);
			}
		}
	}

	private static void tickOne(ServerLevel level, State state) {
		Mob mob = state.mob;
		LivingEntity target = mob.getTarget();
		if (!(target instanceof ServerPlayer player) || !player.isAlive()) {
			state.telegraph = 0;
			return;
		}
		if (state.telegraph > 0) {
			state.telegraph--;
			if (state.destination != null && state.telegraph % 4 == 0) {
				level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, state.destination.x, state.destination.y + 0.1, state.destination.z, 6, 0.3, 0.1, 0.3, 0.01);
			}
			if (state.telegraph == 0 && state.destination != null) {
				if (isFree(level, mob, state.destination)) {
					mob.teleportTo(state.destination.x, state.destination.y, state.destination.z);
					level.playSound(null, state.destination.x, state.destination.y, state.destination.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.6F);
				}
				state.destination = null;
				state.cooldown = COOLDOWN_TICKS;
			}
			return;
		}
		if (--state.cooldown > 0) {
			return;
		}
		double distance = mob.distanceTo(player);
		if (distance < MIN_DISTANCE || distance > MAX_DISTANCE) {
			state.cooldown = 20;
			return;
		}
		Vec3 look = player.getLookAngle();
		Vec3 flat = new Vec3(look.x, 0.0, look.z);
		if (flat.lengthSqr() < 1.0E-4) {
			state.cooldown = 20;
			return;
		}
		Vec3 destination = player.position().subtract(flat.normalize().scale(BEHIND_OFFSET));
		if (!isFree(level, mob, destination)) {
			state.cooldown = 40;
			return;
		}
		state.destination = destination;
		state.telegraph = TELEGRAPH_TICKS;
		Fx.actionbar(player, Component.literal("Sientes una presencia a tu espalda…").withStyle(net.minecraft.ChatFormatting.DARK_GRAY, net.minecraft.ChatFormatting.ITALIC));
		level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENDERMAN_STARE, SoundSource.HOSTILE, 1.0F, 0.5F);
	}

	private static boolean isFree(ServerLevel level, Mob mob, Vec3 destination) {
		Vec3 delta = destination.subtract(mob.position());
		return level.noCollision(mob, mob.getBoundingBox().move(delta));
	}
}
