package com.purgatorio.coretest;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

final class ItemStackHelper {
	private ItemStackHelper() {
	}

	/** Dano del primer objeto danado del inventario. */
	static int damage(ServerPlayer p) {
		for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
			ItemStack s = p.getInventory().getItem(i);
			if (s.isDamaged()) {
				return s.getDamageValue();
			}
		}
		return 0;
	}
}
