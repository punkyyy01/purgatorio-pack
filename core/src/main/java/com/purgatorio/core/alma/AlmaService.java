package com.purgatorio.core.alma;

import net.minecraft.server.level.ServerPlayer;

/**
 * Unica puerta de entrada al Alma. Toda la logica de valores esta en {@link AlmaRules}; este
 * servicio solo orquesta almacenamiento y pantalla. Los valores son centesimas de Alma.
 */
public final class AlmaService {
	private final AlmaStorage storage;
	private final AlmaDisplay display;

	public AlmaService(AlmaStorage storage, AlmaDisplay display) {
		this.storage = storage;
		this.display = display;
	}

	/** Alma actual en centesimas. */
	public int getCentis(ServerPlayer player) {
		return storage.get(player);
	}

	/** Alma actual en puntos (con decimales). */
	public double get(ServerPlayer player) {
		return getCentis(player) / (double) AlmaRules.SCALE;
	}

	/** Fija el Alma (saturada a 0..100). Devuelve el valor final en centesimas. */
	public int set(ServerPlayer player, int centis) {
		int value = AlmaRules.clamp(centis);
		storage.set(player, value);
		display.show(player, value);
		return value;
	}

	/** Suma Alma. Devuelve cuanto se aplico de verdad (puede ser menos si se llego al tope). */
	public int add(ServerPlayer player, int centis) {
		if (centis <= 0) {
			return 0;
		}
		int before = getCentis(player);
		int after = set(player, AlmaRules.add(before, centis));
		return after - before;
	}

	/** Resta Alma sin condiciones (no es un gasto). Devuelve cuanto se quito de verdad. */
	public int remove(ServerPlayer player, int centis) {
		if (centis <= 0) {
			return 0;
		}
		int before = getCentis(player);
		int after = set(player, AlmaRules.add(before, -(long) centis));
		return before - after;
	}

	/** Gasta Alma de forma atomica: si no alcanza no cambia nada y devuelve false. */
	public boolean spend(ServerPlayer player, int centis) {
		int before = getCentis(player);
		if (!AlmaRules.canSpend(before, centis)) {
			return false;
		}
		set(player, before - centis);
		return true;
	}

	/** Aplica la perdida por muerte (~30%). Devuelve las centesimas perdidas. */
	public int loseOnDeath(ServerPlayer player) {
		int before = getCentis(player);
		int lost = AlmaRules.lostOnDeath(before);
		set(player, before - lost);
		return lost;
	}

	/** Multiplicador de dano actual del jugador (1.0 .. 1.10). */
	public double damageMultiplier(ServerPlayer player) {
		return AlmaRules.damageMultiplier(getCentis(player));
	}

	/** Reenvia la barra (al entrar, reaparecer o cambiar de dimension). */
	public void refresh(ServerPlayer player) {
		display.show(player, getCentis(player));
	}
}
