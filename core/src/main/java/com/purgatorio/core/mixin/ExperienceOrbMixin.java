package com.purgatorio.core.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No se generan orbes de XP: ningun origen vanilla (mobs, minado, hornos, botellas...) da Alma. */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
	@Inject(method = "award", at = @At("HEAD"), cancellable = true)
	private static void purgatorio$noOrbs(ServerLevel level, Vec3 pos, int amount, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "awardWithDirection", at = @At("HEAD"), cancellable = true)
	private static void purgatorio$noDirectedOrbs(ServerLevel level, Vec3 pos, Vec3 roughDirection, int amount, CallbackInfo ci) {
		ci.cancel();
	}
}
