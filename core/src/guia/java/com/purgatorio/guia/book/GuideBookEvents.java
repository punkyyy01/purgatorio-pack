package com.purgatorio.guia.book;

import com.purgatorio.guia.gui.HubGui;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/** Todo lo que mantiene el libro con su dueno: darlo, abrirlo y que no salga de su inventario. */
public final class GuideBookEvents {
	/** Cada cuantos ticks se comprueba que todos llevan su libro (red de seguridad). */
	static final int CHECK_INTERVAL = 10;

	private GuideBookEvents() {
	}

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> GuideBook.ensure(handler.getPlayer()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> GuideBook.ensure(newPlayer));

		// Antes de morir: el libro no cae al suelo ni entra en una tumba; reaparece con el jugador.
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player) {
				GuideBook.strip(player);
			}
			return true;
		});

		// Cualquier libro que llegue a ser un objeto suelto (tirado con Q, desbordado...) desaparece.
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity instanceof ItemEntity item && GuideBook.is(item.getItem())) {
				item.discard();
			}
		});

		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack held = player.getItemInHand(hand);
			if (!GuideBook.is(held)) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer serverPlayer) {
				HubGui.open(serverPlayer);
			}
			return InteractionResult.SUCCESS;
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % CHECK_INTERVAL == 0) {
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					GuideBook.ensure(player);
				}
			}
		});
	}
}
