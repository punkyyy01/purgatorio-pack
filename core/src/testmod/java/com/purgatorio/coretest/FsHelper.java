package com.purgatorio.coretest;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

final class FsHelper {
	private FsHelper() {
	}

	static void holdFreshSword(ServerPlayer p) {
		ForgeSuite.giveSword(p, 0);
		p.setItemInHand(InteractionHand.MAIN_HAND, p.getMainHandItem());
	}
}
