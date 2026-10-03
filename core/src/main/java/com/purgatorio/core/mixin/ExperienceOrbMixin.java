package com.purgatorio.core.mixin;

import com.purgatorio.core.alma.MendingGate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * El XP vanilla no da Alma ni niveles, pero las orbes se conservan para Mending: solo se crean si hay un jugador cerca
 * con un objeto reparable. Todas las fuentes (mobs, minado, hornos, botellas, comercio...) pasan por este metodo
 * ({@code award} delega en el).
 */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
	@Inject(method = "awardWithDirection", at = @At("HEAD"), cancellable = true)
	private static void purgatorio$orbsOnlyForMending(ServerLevel level, Vec3 pos, Vec3 roughDirection, int amount, CallbackInfo ci) {
		if (!MendingGate.anyoneNearNeedsRepair(level, pos)) {
			ci.cancel();
		}
	}
}
