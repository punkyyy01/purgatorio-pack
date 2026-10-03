package com.purgatorio.core.forge;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaEvents;
import com.purgatorio.core.item.Upgradeable;
import com.purgatorio.core.menu.Ui;
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
		gui.setTitle(Ui.title("Forja"));
		render(gui, player, onBack, null);
		gui.open();
		return gui;
	}

	private static void render(SimpleGui gui, ServerPlayer player, Runnable onBack, Component lastMessage) {
		Ui.fillAll(gui, 27);
		ItemStack held = player.getMainHandItem();
		ForgeService.Preview preview = PurgatorioCore.forgeService().preview(player, held);

		if (!preview.upgradable()) {
			gui.setSlot(13, new GuiElementBuilder(Items.BARRIER)
				.setName(Ui.text("Nada que mejorar", ChatFormatting.RED).withStyle(ChatFormatting.BOLD))
				.addLoreLine(Ui.text("Sostén en la mano un objeto mejorable", ChatFormatting.GRAY))
				.addLoreLine(Ui.text("y vuelve a abrir la forja.", ChatFormatting.GRAY)));
		} else {
			GuiElementBuilder item = new GuiElementBuilder(held.copy());
			item.addLoreLine(Ui.blank());
			item.addLoreLine(Ui.text("Nivel actual: " + preview.currentLevel() + "/" + preview.recipe().maxLevel(), ChatFormatting.GOLD));
			gui.setSlot(4, item);
			if (preview.maxed()) {
				gui.setSlot(13, new GuiElementBuilder(Items.NETHER_STAR)
					.setName(Ui.text("Al máximo", ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD))
					.addLoreLine(Ui.text("Este objeto ya no admite más mejoras.", ChatFormatting.GRAY)));
			} else {
				ChatFormatting almaColor = preview.enoughAlma() ? ChatFormatting.GREEN : ChatFormatting.RED;
				gui.setSlot(10, new GuiElementBuilder(Items.BOOK)
					.setName(Ui.text("Requisitos", ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD))
					.addLoreLine(Ui.text("Alma y materiales. Se cobra todo", ChatFormatting.GRAY))
					.addLoreLine(Ui.text("junto, o no se cobra nada.", ChatFormatting.GRAY)));
				gui.setSlot(11, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
					.setName(Ui.text("Coste de Alma: " + preview.next().alma(), almaColor).withStyle(ChatFormatting.BOLD))
					.addLoreLine(Ui.text("Tienes: " + AlmaEvents.format(preview.almaHaveCentis()), ChatFormatting.GRAY))
					.addLoreLine(Ui.blank())
					.addLoreLine(Ui.text("Mientras esté gastada, tu bonus de daño", ChatFormatting.DARK_GRAY))
					.addLoreLine(Ui.text("baja ~" + Ui.num(preview.next().alma() / 10.0) + " %. La mejora no se pierde.", ChatFormatting.DARK_GRAY)));
				int slot = 12;
				for (ForgeService.MaterialStatus status : preview.materials()) {
					ItemStack icon = status.item() == null ? new ItemStack(Items.BARRIER) : new ItemStack(status.item());
					ChatFormatting color = status.enough() ? ChatFormatting.GREEN : ChatFormatting.RED;
					gui.setSlot(slot++, new GuiElementBuilder(icon)
						.setName(icon.getHoverName().copy().append(" x" + status.material().count()).withStyle(color))
						.addLoreLine(Ui.text(status.enough() ? "✔ Lo tienes (" + status.have() + ")" : "✘ Te faltan (tienes " + status.have() + ")", color)));
				}
				GuiElementBuilder button = new GuiElementBuilder(preview.canUpgrade() ? Items.ANVIL : Items.BARRIER)
					.setName(Ui.text(preview.canUpgrade() ? "Mejorar a nivel " + (preview.currentLevel() + 1) : "Faltan requisitos",
						preview.canUpgrade() ? ChatFormatting.GREEN : ChatFormatting.GRAY).withStyle(ChatFormatting.BOLD));
				if (held.getItem() instanceof Upgradeable up) {
					up.upgradePreview(preview.currentLevel(), preview.currentLevel() + 1).forEach(button::addLoreLine);
				}
				button.addLoreLine(Ui.blank());
				button.addLoreLine(Ui.text(preview.canUpgrade() ? "▶ Clic para mejorar" : "Revisa Alma y materiales a la izquierda.",
					preview.canUpgrade() ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY));
				button.setCallback((index, type, action, gui2) -> {
					ForgeService.Outcome outcome = PurgatorioCore.forgeService().tryUpgrade(player, player.getMainHandItem());
					player.sendSystemMessage(outcome.message().copy().withStyle(outcome.ok() ? ChatFormatting.GREEN : ChatFormatting.RED));
					render(gui, player, onBack, outcome.message());
				});
				gui.setSlot(22, button);
			}
		}
		if (lastMessage != null) {
			gui.setSlot(8, new GuiElementBuilder(Items.PAPER).setName(lastMessage.copy().withStyle(ChatFormatting.WHITE)));
		}
		if (onBack != null) {
			gui.setSlot(18, new GuiElementBuilder(Items.ARROW)
				.setName(Ui.text("Volver", ChatFormatting.GRAY))
				.setCallback((index, type, action, gui2) -> onBack.run()));
		}
	}
}
