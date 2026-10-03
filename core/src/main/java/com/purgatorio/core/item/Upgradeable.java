package com.purgatorio.core.item;

import net.minecraft.world.item.ItemStack;

/** Un item que la forja puede mejorar. El item decide que cambia en cada nivel. */
public interface Upgradeable {
	/** Aplica las estadisticas del nivel {@code level} al stack (el nivel ya se guarda en {@link UpgradeData}). */
	void applyLevel(ItemStack stack, int level);
}
