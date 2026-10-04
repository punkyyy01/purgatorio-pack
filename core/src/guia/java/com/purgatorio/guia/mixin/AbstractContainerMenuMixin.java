package com.purgatorio.guia.mixin;

import com.purgatorio.guia.book.GuideBookGuard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cancela los clics de contenedor que sacarian el libro-guia del inventario del jugador. */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
	@Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
	private void guia$keepBook(int slotId, int button, ContainerInput input, Player player, CallbackInfo ci) {
		AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
		if (player instanceof ServerPlayer && GuideBookGuard.blocks(self, slotId, button, input, player)) {
			ci.cancel();
			self.sendAllDataToRemote();   // el cliente ya movio el objeto en su pantalla: se le devuelve el estado real
		}
	}
}
