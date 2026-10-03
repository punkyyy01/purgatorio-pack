package com.purgatorio.core.alma;

/**
 * Reglas puras de Alma. No depende de Minecraft: todo se prueba con JUnit.
 *
 * <p>Unidad interna: centesimas de Alma (0..{@link #MAX}). 1 punto de Alma = 100 centesimas.
 * Las centesimas evitan errores de coma flotante y permiten ganancias fraccionarias.
 *
 * <p>Alma NO es un sistema de niveles: tope absoluto de 100 y de +10% de dano.
 */
public final class AlmaRules {
	/** Centesimas por punto de Alma. */
	public static final int SCALE = 100;
	/** Maximo absoluto (100 de Alma) en centesimas. */
	public static final int MAX = 100 * SCALE;
	/** Bonus de dano maximo, alcanzado con Alma 100. Techo absoluto. */
	public static final double MAX_BONUS = 0.10;
	/** Porcentaje de Alma que se conserva al morir (se pierde ~30%). */
	public static final int DEATH_KEEP_PERCENT = 70;

	private AlmaRules() {
	}

	/** Convierte puntos enteros de Alma a centesimas (saturado en el rango valido). */
	public static int fromPoints(int points) {
		return clamp((long) points * SCALE);
	}

	/** Satura al rango valido 0..{@link #MAX}. */
	public static int clamp(long centis) {
		if (centis < 0) {
			return 0;
		}
		return (int) Math.min(centis, MAX);
	}

	/** Suma (o resta si {@code delta} es negativo) con saturacion. Nunca desborda ni supera el tope. */
	public static int add(int current, long delta) {
		return clamp((long) clamp(current) + delta);
	}

	/** Multiplicador de dano lineal: 1.0 con Alma 0, 1.05 con 50, 1.10 con 100. Nunca mayor que 1.10. */
	public static double damageMultiplier(int centis) {
		double bonus = (double) clamp(centis) / MAX * MAX_BONUS;
		return 1.0 + Math.min(bonus, MAX_BONUS);
	}

	/** Alma que queda tras morir: conserva el 70% (100 -> 70, 70 -> 49). */
	public static int afterDeath(int centis) {
		return (int) ((long) clamp(centis) * DEATH_KEEP_PERCENT / 100);
	}

	/** Alma perdida al morir (para depositarla mas adelante en la tumba). */
	public static int lostOnDeath(int centis) {
		return clamp(centis) - afterDeath(centis);
	}

	/** Si hay Alma suficiente para pagar {@code cost} (> 0). */
	public static boolean canSpend(int current, int cost) {
		return cost >= 0 && clamp(current) >= cost;
	}

	/** Nivel mostrado en la barra de XP: parte entera del Alma. */
	public static int displayLevel(int centis) {
		return clamp(centis) / SCALE;
	}

	/** Progreso de la barra de XP (0..1): fraccion hacia el siguiente punto. Con Alma 100 la barra va llena. */
	public static float displayProgress(int centis) {
		int c = clamp(centis);
		if (c >= MAX) {
			return 1.0F;
		}
		return (c % SCALE) / (float) SCALE;
	}
}
