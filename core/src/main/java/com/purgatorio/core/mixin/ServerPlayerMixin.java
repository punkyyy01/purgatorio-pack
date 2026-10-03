package com.purgatorio.core.mixin;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * La barra de XP vanilla muestra Alma, no XP. Vanilla reenvia el paquete en el login, al
 * reaparecer y al cambiar de dimension: se sustituyen los valores. El XP vanilla no puede
 * aumentar (ni dar Alma): se anulan las ganancias directas.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	@Redirect(
		method = "doTick",
		at = @At(value = "NEW", target = "(FII)Lnet/minecraft/network/protocol/game/ClientboundSetExperiencePacket;")
	)
	private ClientboundSetExperiencePacket purgatorio$almaInsteadOfXp(float progress, int total, int level) {
		int centis = PurgatorioCore.alma().getCentis((ServerPlayer) (Object) this);
		int almaLevel = AlmaRules.displayLevel(centis);
		return new ClientboundSetExperiencePacket(AlmaRules.displayProgress(centis), almaLevel, almaLevel);
	}

	@Inject(method = "giveExperiencePoints", at = @At("HEAD"), cancellable = true)
	private void purgatorio$noVanillaXpPoints(int amount, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "giveExperienceLevels", at = @At("HEAD"), cancellable = true)
	private void purgatorio$noVanillaXpLevels(int amount, CallbackInfo ci) {
		ci.cancel();
	}
}
