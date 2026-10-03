package com.purgatorio.core.feedback;

import com.purgatorio.core.PurgatorioCore;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

/** Concede entradas del Diario (logros purgatorio:diario/*) desde el codigo, p. ej. al derrotar a un enemigo. */
public final class Diary {
	private Diary() {
	}

	/** Concede la entrada {@code diario/<path>} si aun no la tiene. Devuelve true si se concedio ahora. */
	public static boolean award(ServerPlayer player, String path) {
		AdvancementHolder holder = player.level().getServer().getAdvancements().get(PurgatorioCore.id("diario/" + path));
		if (holder == null) {
			return false;
		}
		var progress = player.getAdvancements().getOrStartProgress(holder);
		if (progress.isDone()) {
			return false;
		}
		boolean granted = false;
		for (String criterion : progress.getRemainingCriteria()) {
			granted |= player.getAdvancements().award(holder, criterion);
		}
		return granted;
	}
}
