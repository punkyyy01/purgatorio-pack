package com.purgatorio.core.forge;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaEvents;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Pantalla de la forja. Mejora el objeto que el jugador sostiene en la mano principal. */
public final class ForgeGui {
	private ForgeGui() {
	}

	public static SimpleGui open(ServerPlayer player, Runnable onBack) {
		SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
		gui.setTitle(Component.literal("Forja"));
		render(gui, player, onBack, null);
		gui.open();
		return gui;
	}

	private static void render(SimpleGui gui, ServerPlayer player, Runnable onBack, Component lastMessage) {
		for (int i = 0; i < 27; i++) {
			gui.clearSlot(i);
		}
		ItemStack held = player.getMainHandItem();
		ForgeService.Preview preview = PurgatorioCore.forgeService().preview(player, held);

		if (!preview.upgradable()) {
			gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
				.setName(Component.literal("Nada que mejorar").withStyle(ChatFormatting.RED))
				.addLoreLine(Component.literal("Sostén en la mano un objeto mejorable").withStyle(ChatFormatting.GRAY))
				.addLoreLine(Component.literal("y vuelve a abrir la forja.").withStyle(ChatFormatting.GRAY)));
		} else {
			gui.setSlot(4, new GuiElementBuilder(held.copy())
				.addLoreLine(Component.literal("Nivel actual: " + preview.currentLevel() + "/" + preview.recipe().maxLevel()).withStyle(ChatFormatting.GOLD)));
			if (preview.maxed()) {
				gui.setSlot(13, new GuiElementBuilder(Items.NETHER_STAR)
					.setName(Component.literal("Al máximo").withStyle(ChatFormatting.GOLD))
					.addLoreLine(Component.literal("Este objeto ya no admite más mejoras.").withStyle(ChatFormatting.GRAY)));
			} else {
				ChatFormatting almaColor = preview.enoughAlma() ? ChatFormatting.GREEN : ChatFormatting.RED;
				gui.setSlot(11, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
					.setName(Component.literal("Coste de Alma: " + preview.next().alma()).withStyle(almaColor))
					.addLoreLine(Component.literal("Tienes: " + AlmaEvents.format(preview.almaHaveCentis())).withStyle(ChatFormatting.GRAY))
					.addLoreLine(Component.literal("Gastarla baja tu bonus de daño ~" + (preview.next().alma() / 10.0) + "%,").withStyle(ChatFormatting.DARK_GRAY))
					.addLoreLine(Component.literal("pero la mejora es permanente.").withStyle(ChatFormatting.DARK_GRAY)));
				int slot = 12;
				for (ForgeService.MaterialStatus status : preview.materials()) {
					ItemStack icon = status.item() == null ? new ItemStack(Items.BARRIER) : new ItemStack(status.item());
					ChatFormatting color = status.enough() ? ChatFormatting.GREEN : ChatFormatting.RED;
					gui.setSlot(slot++, new GuiElementBuilder(icon)
						.setName(icon.getHoverName().copy().append(" x" + status.material().count()).withStyle(color))
						.addLoreLine(Component.literal("Tienes: " + status.have()).withStyle(ChatFormatting.GRAY)));
				}
				gui.setSlot(22, new GuiElementBuilder(preview.canUpgrade() ? Items.ANVIL : Items.BARRIER)
					.setName(Component.literal(preview.canUpgrade() ? "Mejorar" : "Faltan requisitos")
						.withStyle(preview.canUpgrade() ? ChatFormatting.GREEN : ChatFormatting.GRAY))
					.setCallback((index, type, action, gui2) -> {
						ForgeService.Outcome outcome = PurgatorioCore.forgeService().tryUpgrade(player, player.getMainHandItem());
						player.sendSystemMessage(outcome.message().copy().withStyle(outcome.ok() ? ChatFormatting.GREEN : ChatFormatting.RED));
						render(gui, player, onBack, outcome.message());
					}));
			}
		}
		if (lastMessage != null) {
			gui.setSlot(8, new GuiElementBuilder(Items.PAPER).setName(lastMessage));
		}
		if (onBack != null) {
			gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
				.setName(Component.literal("Volver").withStyle(ChatFormatting.GRAY))
				.setCallback((index, type, action, gui2) -> onBack.run()));
		}
	}
}
