package com.purgatorio.guia.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Cofre doble con una lista paginada (36 por pagina), un boton de volver y hasta tres filtros que se cambian con clic
 * (clic derecho = el anterior). Base de los catalogos y de la guia del servidor.
 */
public abstract class PagedGui {
	public static final int PAGE_SIZE = 36;
	public static final int FIRST_ENTRY = 9;
	public static final int LAST_ENTRY = 44;
	public static final int BACK_SLOT = 45;
	public static final int FILTER_1 = 47;
	public static final int FILTER_2 = 48;
	public static final int FILTER_3 = 49;
	public static final int PREV_SLOT = 51;
	public static final int INFO_SLOT = 52;
	public static final int NEXT_SLOT = 53;

	/** Opcion de un filtro: la etiqueta que ve el jugador y el valor que usa la logica van JUNTOS, para que no se desincronicen. */
	public record Option<T>(String label, T value) {
	}

	protected final ServerPlayer player;
	protected final SimpleGui gui;
	private final Runnable onBack;
	protected int page;

	protected PagedGui(ServerPlayer player, String title, Runnable onBack) {
		this.player = player;
		this.onBack = onBack;
		this.gui = new SimpleGui(MenuType.GENERIC_9x6, player, false);
		gui.setTitle(Ui.title(title));
	}

	/** Todas las entradas que pasan los filtros actuales. */
	protected abstract List<GuiElementBuilder> entries();

	/** Pone los botones de filtro (con {@link #cycle}). Por defecto, ninguno. */
	protected void addFilters() {
	}

	public SimpleGui gui() {
		return gui;
	}

	public void open() {
		render();
		gui.open();
	}

	protected void render() {
		for (int i = 0; i < 54; i++) {
			gui.clearSlot(i);
		}
		for (int i = 0; i < 9; i++) {
			gui.setSlot(i, Ui.filler());
			gui.setSlot(45 + i, Ui.filler());
		}
		gui.setSlot(BACK_SLOT, onBack != null
			? new GuiElementBuilder(Items.ARROW).setName(Ui.text("◀ Menú principal", ChatFormatting.YELLOW))
				.setCallback((index, type, action, g) -> onBack.run())
			: new GuiElementBuilder(Items.BARRIER).setName(Ui.text("Cerrar", ChatFormatting.RED))
				.setCallback((index, type, action, g) -> gui.close()));
		addFilters();

		List<GuiElementBuilder> entries = entries();
		int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		page = Math.min(page, pages - 1);
		if (entries.isEmpty()) {
			gui.setSlot(22, new GuiElementBuilder(Items.PAPER)
				.setName(Ui.text("Nada que mostrar", ChatFormatting.GRAY).withStyle(ChatFormatting.BOLD))
				.addLoreLine(Ui.text("Prueba con otros filtros.", ChatFormatting.GRAY)));
			return;
		}
		int from = page * PAGE_SIZE;
		for (int i = 0; i < PAGE_SIZE && from + i < entries.size(); i++) {
			gui.setSlot(FIRST_ENTRY + i, entries.get(from + i));
		}
		gui.setSlot(INFO_SLOT, new GuiElementBuilder(Items.OAK_SIGN)
			.setName(Ui.text(entries.size() + " en total", ChatFormatting.YELLOW))
			.addLoreLine(Ui.text("Página " + (page + 1) + " de " + pages, ChatFormatting.GRAY)));
		if (page > 0) {
			gui.setSlot(PREV_SLOT, new GuiElementBuilder(Items.ARROW).setName(Ui.text("◀ Anterior", ChatFormatting.YELLOW))
				.setCallback((index, type, action, g) -> {
					page--;
					render();
				}));
		}
		if (page < pages - 1) {
			gui.setSlot(NEXT_SLOT, new GuiElementBuilder(Items.ARROW).setName(Ui.text("Siguiente ▶", ChatFormatting.YELLOW))
				.setCallback((index, type, action, g) -> {
					page++;
					render();
				}));
		}
	}

	/** Boton de filtro: lista las opciones con la actual marcada; clic = siguiente, clic derecho = anterior. */
	protected <T> void cycle(int slot, Item icon, String title, List<T> options, int current, IntConsumer set, Function<T, String> label) {
		GuiElementBuilder b = new GuiElementBuilder(icon).hideDefaultTooltip()
			.setName(Ui.text(title, ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
		for (int i = 0; i < options.size(); i++) {
			boolean now = i == current;
			b.addLoreLine(Ui.text((now ? "▶ " : "   ") + label.apply(options.get(i)), now ? ChatFormatting.YELLOW : ChatFormatting.DARK_GRAY));
		}
		b.addLoreLine(Ui.blank());
		b.addLoreLine(Ui.text("Clic: siguiente · Clic derecho: anterior", ChatFormatting.DARK_GRAY));
		b.setCallback((index, type, action, g) -> {
			set.accept(Math.floorMod(current + (type == ClickType.MOUSE_RIGHT ? -1 : 1), options.size()));
			page = 0;
			render();
		});
		gui.setSlot(slot, b);
	}
}
