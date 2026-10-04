package com.purgatorio.guia.gui;

import com.purgatorio.guia.inspect.EffectEntries;
import com.purgatorio.guia.inspect.EnchantmentEntries;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Inspector de objetos: cofre doble. Arriba, el objeto elegido y una entrada por encantamiento; abajo se ve el
 * inventario del jugador y un clic en cualquier objeto lo elige. El objeto NUNCA se mueve: el inspector solo trabaja
 * con una copia, asi que no hay forma de perderlo (cierre, desconexion o caida del servidor incluidos).
 */
public final class InspectorGui {
	static final int TOP_SLOTS = 54;
	/** Zona de entradas: filas 2 a 5 (indices 9-44). */
	static final int ENTRIES_FIRST = 9;
	static final int PAGE_SIZE = 36;
	static final int SELECTED_SLOT = 4;
	static final int CLOSE_SLOT = 45;
	static final int PREV_SLOT = 48;
	static final int PAGE_INFO_SLOT = 49;
	static final int NEXT_SLOT = 50;
	/** El inventario del jugador se refleja a partir del indice 54: 27 de inventario (inv 9-35) y 9 de hotbar (inv 0-8). */
	static final int MIRROR_FIRST = TOP_SLOTS;
	static final int MIRROR_SIZE = 36;

	private final ServerPlayer player;
	private final SimpleGui gui;
	private ItemStack inspected = ItemStack.EMPTY;
	private int page;

	private InspectorGui(ServerPlayer player) {
		this.player = player;
		this.gui = new SimpleGui(MenuType.GENERIC_9x6, player, true);
		gui.setTitle(Ui.title("Inspector de objetos"));
	}

	public static InspectorGui open(ServerPlayer player) {
		InspectorGui inspector = new InspectorGui(player);
		inspector.render();
		inspector.gui.open();
		return inspector;
	}

	public SimpleGui gui() {
		return gui;
	}

	public ItemStack inspected() {
		return inspected;
	}

	/** Elige el objeto a inspeccionar (siempre una copia). */
	public void select(ItemStack stack) {
		inspected = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
		page = 0;
		render();
	}

	private void setPage(int newPage) {
		page = newPage;
		render();
	}

	private void render() {
		for (int i = 0; i < MIRROR_FIRST + MIRROR_SIZE; i++) {
			gui.clearSlot(i);
		}
		for (int i = 0; i < 9; i++) {
			gui.setSlot(i, Ui.filler());
			gui.setSlot(45 + i, Ui.filler());
		}
		gui.setSlot(CLOSE_SLOT, new GuiElementBuilder(Items.BARRIER)
			.setName(Ui.text("Cerrar", ChatFormatting.RED))
			.setCallback((index, type, action, g) -> gui.close()));

		if (inspected.isEmpty()) {
			gui.setSlot(SELECTED_SLOT, new GuiElementBuilder(Items.HOPPER)
				.setName(Ui.text("Elige un objeto", ChatFormatting.YELLOW).withStyle(ChatFormatting.BOLD))
				.addLoreLine(Ui.text("Haz clic en un objeto de tu inventario", ChatFormatting.GRAY))
				.addLoreLine(Ui.text("(abajo) para ver sus encantamientos", ChatFormatting.GRAY))
				.addLoreLine(Ui.text("y qué hace cada uno.", ChatFormatting.GRAY))
				.addLoreLine(Ui.blank())
				.addLoreLine(Ui.text("No se mueve ni se gasta nada.", ChatFormatting.DARK_GRAY)));
		} else {
			gui.setSlot(SELECTED_SLOT, new GuiElementBuilder(inspected.copy())
				.addLoreLine(Ui.blank())
				.addLoreLine(Ui.text("▶ Clic para quitar de la vista", ChatFormatting.YELLOW))
				.setCallback((index, type, action, g) -> select(ItemStack.EMPTY)));
			renderEntries();
		}
		renderMirror();
	}

	private void renderEntries() {
		EnchantmentEntries.Result result = EnchantmentEntries.of(player, inspected);
		List<GuiElementBuilder> effects = EffectEntries.of(player, inspected);
		// Si el objeto tiene encantamientos o da efectos se explica eso; si no, el catalogo de lo que admite.
		boolean catalog = !result.enchanted() && effects.isEmpty();
		List<GuiElementBuilder> entries = new ArrayList<>();
		if (result.enchanted() || catalog) {
			entries.addAll(result.entries());
		}
		entries.addAll(effects);
		int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		page = Math.min(page, pages - 1);
		if (entries.isEmpty()) {
			gui.setSlot(22, new GuiElementBuilder(Items.PAPER)
				.setName(Ui.text("Nada que explicar", ChatFormatting.GRAY).withStyle(ChatFormatting.BOLD))
				.addLoreLine(Ui.text("Este objeto no tiene encantamientos ni", ChatFormatting.GRAY))
				.addLoreLine(Ui.text("efectos, y no admite encantamientos.", ChatFormatting.GRAY)));
			return;
		}
		if (catalog) {
			gui.setSlot(PAGE_INFO_SLOT, new GuiElementBuilder(Items.OAK_SIGN)
				.setName(Ui.text("Sin encantamientos", ChatFormatting.YELLOW).withStyle(ChatFormatting.BOLD))
				.addLoreLine(Ui.text("Estos son los que admite:", ChatFormatting.GRAY))
				.addLoreLine(Ui.text(entries.size() + " en total (página " + (page + 1) + "/" + pages + ")", ChatFormatting.DARK_GRAY)));
		} else if (pages > 1) {
			gui.setSlot(PAGE_INFO_SLOT, new GuiElementBuilder(Items.OAK_SIGN)
				.setName(Ui.text("Página " + (page + 1) + "/" + pages, ChatFormatting.YELLOW)));
		}
		int from = page * PAGE_SIZE;
		for (int i = 0; i < PAGE_SIZE && from + i < entries.size(); i++) {
			gui.setSlot(ENTRIES_FIRST + i, entries.get(from + i));
		}
		if (page > 0) {
			gui.setSlot(PREV_SLOT, new GuiElementBuilder(Items.ARROW)
				.setName(Ui.text("◀ Anterior", ChatFormatting.YELLOW))
				.setCallback((index, type, action, g) -> setPage(page - 1)));
		}
		if (page < pages - 1) {
			gui.setSlot(NEXT_SLOT, new GuiElementBuilder(Items.ARROW)
				.setName(Ui.text("Siguiente ▶", ChatFormatting.YELLOW))
				.setCallback((index, type, action, g) -> setPage(page + 1)));
		}
	}

	/** Refleja el inventario real (solo lectura): cada objeto es un elemento propio cuyo clic lo elige. */
	private void renderMirror() {
		Inventory inv = player.getInventory();
		for (int k = 0; k < MIRROR_SIZE; k++) {
			ItemStack stack = inv.getItem(mirrorToInventory(k));
			if (stack.isEmpty()) {
				continue;
			}
			ItemStack copy = stack.copy();
			gui.setSlot(MIRROR_FIRST + k, new GuiElementBuilder(copy)
				.setCallback((index, type, action, g) -> select(copy)));
		}
	}

	/** Posicion en el reflejo (0-35) -> hueco del inventario: primero las 3 filas de arriba, despues la hotbar. */
	static int mirrorToInventory(int k) {
		return k < 27 ? 9 + k : k - 27;
	}
}
