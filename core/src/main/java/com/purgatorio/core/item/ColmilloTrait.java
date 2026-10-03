package com.purgatorio.core.item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Rasgo Brasa del Colmillo de Ceniza: cada tercer golpe seguido prende al enemigo, a cambio de hambre. */
public final class ColmilloTrait {
	public static final int HITS_TO_IGNITE = 3;
	/** Ventana (ticks) para que los golpes cuenten como "seguidos". */
	public static final int COMBO_WINDOW_TICKS = 60;
	public static final float HUNGER_EXHAUSTION = 4.0F; // 4.0 de agotamiento = 1 punto de hambre

	private record Combo(UUID target, int hits, long lastTick) {
	}

	private static final Map<UUID, Combo> COMBOS = new ConcurrentHashMap<>();

	private ColmilloTrait() {
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((victim, source, baseDamage, damageTaken, blocked) -> {
			if (blocked || damageTaken <= 0.0F) {
				return;
			}
			if (source.getEntity() instanceof ServerPlayer player && source.getDirectEntity() == player) {
				onHit(player, victim, source);
			}
		});
		net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.LEAVE.register(p -> COMBOS.remove(p.getUUID()));
	}

	/** Procesa un golpe cuerpo a cuerpo del jugador. Devuelve true si el rasgo se activo. */
	public static boolean onHit(ServerPlayer player, LivingEntity victim, DamageSource source) {
		ItemStack weapon = player.getMainHandItem();
		if (!(weapon.getItem() instanceof ColmilloDeCeniza)) {
			return false;
		}
		ServerLevel level = player.level();
		long now = level.getGameTime();
		Combo previous = COMBOS.get(player.getUUID());
		int hits = (previous != null && previous.target().equals(victim.getUUID()) && now - previous.lastTick() <= COMBO_WINDOW_TICKS)
			? previous.hits() + 1 : 1;
		if (hits < HITS_TO_IGNITE) {
			COMBOS.put(player.getUUID(), new Combo(victim.getUUID(), hits, now));
			return false;
		}
		COMBOS.remove(player.getUUID());
		victim.igniteForSeconds(ColmilloDeCeniza.burnSeconds(UpgradeData.get(weapon)));
		player.causeFoodExhaustion(HUNGER_EXHAUSTION);
		level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.6F, 1.2F);
		level.sendParticles(ParticleTypes.FLAME, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 12, 0.3, 0.4, 0.3, 0.02);
		player.sendSystemMessage(Component.literal("¡Brasa!"), true);
		return true;
	}
}
