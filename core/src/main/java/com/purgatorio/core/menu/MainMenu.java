package com.purgatorio.core.menu;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaEvents;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.forge.ForgeGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

/** Menu minimo de Purgatorio: Alma, forja y Diario. No es el menu final. */
public final class MainMenu {
	private MainMenu() {
	}

	public static SimpleGui open(ServerPlayer player) {
		SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
		gui.setTitle(Ui.title("Purgatorio"));
		Ui.fillAll(gui, 27);

		int centis = PurgatorioCore.alma().getCentis(player);
		double bonus = (AlmaRules.damageMultiplier(centis) - 1.0) * 100.0;
		gui.setSlot(11, new GuiElementBuilder(Items.EXPERIENCE_BOTTLE)
			.setName(Component.literal("Tu Alma · " + AlmaEvents.format(centis) + " / 100").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
			.addLoreLine(Ui.bar(centis / (double) AlmaRules.MAX, 20, ChatFormatting.GREEN))
			.addLoreLine(Component.literal("Bonus de daño: +" + Ui.num(bonus) + " %").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal("  (máximo +10 %)").withStyle(ChatFormatting.DARK_GRAY)))
			.addLoreLine(Ui.blank())
			.addLoreLine(Ui.text("Se gana descubriendo lugares y derrotando", ChatFormatting.GRAY))
			.addLoreLine(Ui.text("enemigos. Se gasta en la forja.", ChatFormatting.GRAY))
			.addLoreLine(Ui.text("Al morir pierdes ~30 % de tu Alma.", ChatFormatting.RED))
			.addLoreLine(Ui.blank())
			.addLoreLine(Ui.text("El Alma que gastas ya no se pierde.", ChatFormatting.DARK_GRAY)));

		gui.setSlot(13, new GuiElementBuilder(Items.ANVIL)
			.setName(Ui.text("Forja", ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD))
			.addLoreLine(Ui.text("Mejora el objeto que sostienes en la mano.", ChatFormatting.GRAY))
			.addLoreLine(Ui.text("Cuesta Alma y materiales; la mejora es", ChatFormatting.GRAY))
			.addLoreLine(Ui.text("permanente.", ChatFormatting.GRAY))
			.addLoreLine(Ui.blank())
			.addLoreLine(Ui.text("▶ Clic para abrir", ChatFormatting.YELLOW))
			.setCallback((index, type, action, gui2) -> ForgeGui.open(player, () -> open(player))));

		GuiElementBuilder diary = new GuiElementBuilder(Items.WRITABLE_BOOK)
			.setName(Ui.text("Diario", ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
		List<Component> found = discoveries(player);
		if (found.isEmpty()) {
			diary.addLoreLine(Ui.text("Todavía no has descubierto nada.", ChatFormatting.GRAY));
			diary.addLoreLine(Ui.text("Explora: lo que encuentres quedará anotado aquí.", ChatFormatting.DARK_GRAY));
		} else {
			diary.addLoreLine(Ui.text("Descubrimientos: " + found.size(), ChatFormatting.GRAY));
			diary.addLoreLine(Ui.blank());
			found.forEach(diary::addLoreLine);
			diary.addLoreLine(Ui.blank());
			diary.addLoreLine(Ui.text("También en la pestaña de logros (tecla L).", ChatFormatting.DARK_GRAY));
		}
		gui.setSlot(15, diary);

		gui.setSlot(22, new GuiElementBuilder(Items.BARRIER)
			.setName(Ui.text("Cerrar", ChatFormatting.RED))
			.setCallback((index, type, action, gui2) -> gui.close()));
		gui.open();
		return gui;
	}

	/** Solo lo YA descubierto: el Diario no revela lugares que el jugador no ha visto. */
	public static List<Component> discoveries(ServerPlayer player) {
		List<Component> out = new ArrayList<>();
		for (AdvancementHolder holder : player.level().getServer().getAdvancements().getAllAdvancements()) {
			String path = holder.id().getPath();
			if (!holder.id().getNamespace().equals(PurgatorioCore.NAMESPACE) || !path.startsWith("diario/") || path.equals("diario/raiz")) {
				continue;
			}
			if (player.getAdvancements().getOrStartProgress(holder).isDone()) {
				DisplayInfo display = holder.value().display().orElse(null);
				if (display != null) {
					out.add(Component.literal("✦ ").withStyle(ChatFormatting.AQUA).append(display.title().copy().withStyle(ChatFormatting.WHITE)));
				}
			}
		}
		return out;
	}
}
