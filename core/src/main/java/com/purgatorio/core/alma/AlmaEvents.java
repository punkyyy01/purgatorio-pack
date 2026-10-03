package com.purgatorio.core.alma;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

/** Engancha el Alma a los eventos del servidor (entrar, morir, reaparecer). */
public final class AlmaEvents {
	/**
	 * Se dispara cuando un jugador pierde Alma por morir. Gancho previsto para depositar el Alma
	 * derramada en la tumba (Universal Graves); todavia no hay ningun oyente.
	 */
	public static final Event<LostOnDeath> LOST_ON_DEATH = EventFactory.createArrayBacked(
		LostOnDeath.class,
		listeners -> (player, lostCentis, source) -> {
			for (LostOnDeath listener : listeners) {
				listener.onLostOnDeath(player, lostCentis, source);
			}
		}
	);

	/** Se dispara cuando un jugador gana Alma (cantidad realmente aplicada, en centesimas). */
	public static final Event<Gained> GAINED = EventFactory.createArrayBacked(
		Gained.class,
		listeners -> (player, gainedCentis, reason) -> {
			for (Gained listener : listeners) {
				listener.onGained(player, gainedCentis, reason);
			}
		}
	);

	@FunctionalInterface
	public interface Gained {
		void onGained(ServerPlayer player, int gainedCentis, com.purgatorio.core.feedback.AlmaReason reason);
	}

	@FunctionalInterface
	public interface LostOnDeath {
		void onLostOnDeath(ServerPlayer player, int lostCentis, DamageSource source);
	}

	/** Avisos pendientes de mostrar al reaparecer (jugador muerto -> centesimas perdidas). */
	private static final Map<UUID, Integer> PENDING_NOTICE = new ConcurrentHashMap<>();

	private AlmaEvents() {
	}

	public static void register(AlmaService alma) {
		ServerPlayerEvents.JOIN.register(alma::refresh);

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayer player) {
				int lost = alma.loseOnDeath(player);
				if (lost > 0) {
					PENDING_NOTICE.put(player.getUUID(), lost);
				}
				LOST_ON_DEATH.invoker().onLostOnDeath(player, lost, source);
			}
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			alma.refresh(newPlayer);
			Integer lost = PENDING_NOTICE.remove(oldPlayer.getUUID());
			if (lost != null) {
				com.purgatorio.core.feedback.AlmaFeedback.onDeathLoss(newPlayer, lost, alma.getCentis(newPlayer));
				newPlayer.sendSystemMessage(Component.literal("Tu Alma se derrama... pierdes " + format(lost) + " (te quedan "
					+ format(alma.getCentis(newPlayer)) + ")."));
			}
		});

		ServerPlayerEvents.LEAVE.register(player -> PENDING_NOTICE.remove(player.getUUID()));
	}

	/** Formatea centesimas como puntos de Alma ("12" o "12,5"). */
	public static String format(int centis) {
		if (centis % AlmaRules.SCALE == 0) {
			return Integer.toString(centis / AlmaRules.SCALE);
		}
		return String.format("%.2f", centis / (double) AlmaRules.SCALE).replaceAll("0+$", "").replace('.', ',');
	}
}
