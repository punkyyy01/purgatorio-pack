package com.purgatorio.guia.book;

import com.purgatorio.guia.gui.Ui;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

/**
 * El libro-guia: un libro de conocimiento vanilla marcado con datos propios (los clientes no necesitan nada).
 * Cada jugador tiene siempre exactamente uno; no se puede tirar ni guardar (ver {@link GuideBookGuard}).
 */
public final class GuideBook {
	/** Hueco preferido: el ultimo del inventario (no de la hotbar, para no quitar un hueco util). */
	public static final int PREFERRED_SLOT = 35;
	private static final String MARKER = "purgatorio_guia";
	private static final CompoundTag MARKER_TAG = new CompoundTag();

	static {
		MARKER_TAG.putBoolean(MARKER, true);
	}

	private GuideBook() {
	}

	public static ItemStack create() {
		ItemStack stack = new ItemStack(Items.KNOWLEDGE_BOOK);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(MARKER_TAG.copy()));
		stack.set(DataComponents.CUSTOM_NAME, Ui.title("Guía del Purgatorio"));
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		stack.set(DataComponents.LORE, new ItemLore(List.of(
			Ui.text("Explica los objetos del servidor:", ChatFormatting.GRAY),
			Ui.text("encantamientos y qué hace cada uno.", ChatFormatting.GRAY),
			Ui.blank(),
			Ui.text("Clic derecho para abrir (o escribe /guia).", ChatFormatting.YELLOW),
			Ui.text("Siempre vuelve contigo: no se puede tirar.", ChatFormatting.DARK_GRAY))));
		return stack;
	}

	public static boolean is(ItemStack stack) {
		return !stack.isEmpty() && stack.is(Items.KNOWLEDGE_BOOK)
			&& stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).matchedBy(MARKER_TAG);
	}

	/** Cuantos libros-guia lleva el jugador (inventario, equipo y cursor). */
	public static int count(ServerPlayer player) {
		Inventory inv = player.getInventory();
		int n = is(player.containerMenu.getCarried()) ? 1 : 0;
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (is(inv.getItem(i))) {
				n++;
			}
		}
		return n;
	}

	/**
	 * Deja al jugador con exactamente un libro: lo da si falta y quita los sobrantes (duplicados por modo creativo,
	 * comandos...). Si el inventario esta lleno no se desplaza nada: se reintenta en el siguiente ciclo.
	 * @return true si cambio algo
	 */
	public static boolean ensure(ServerPlayer player) {
		Inventory inv = player.getInventory();
		int found = is(player.containerMenu.getCarried()) ? 1 : 0;
		boolean changed = false;
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (is(inv.getItem(i))) {
				if (++found > 1) {
					inv.setItem(i, ItemStack.EMPTY);
					changed = true;
				}
			}
		}
		if (found == 0) {
			changed = give(inv);
		}
		return changed;
	}

	/** Pone el libro en el hueco preferido o, si esta ocupado, en el primer hueco libre empezando por el final. */
	private static boolean give(Inventory inv) {
		for (int i = PREFERRED_SLOT; i >= 0; i--) {
			if (inv.getItem(i).isEmpty()) {
				inv.setItem(i, create());
				return true;
			}
		}
		return false;
	}

	/** Quita todos los libros (antes de morir: no deben caer ni acabar en una tumba). */
	public static void strip(ServerPlayer player) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (is(inv.getItem(i))) {
				inv.setItem(i, ItemStack.EMPTY);
			}
		}
		if (is(player.containerMenu.getCarried())) {
			player.containerMenu.setCarried(ItemStack.EMPTY);
		}
	}
}
