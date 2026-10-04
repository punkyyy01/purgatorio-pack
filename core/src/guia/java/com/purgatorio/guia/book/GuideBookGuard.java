package com.purgatorio.guia.book;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

/**
 * Reglas de clic que impiden sacar el libro del inventario del jugador. Se pueden mover libremente DENTRO de su
 * inventario; lo que se bloquea es todo lo que lo llevaria a otro contenedor o al suelo. Lo que se escape de aqui
 * (creativo, comandos, otros mods) lo corrige {@link GuideBook#ensure}.
 */
public final class GuideBookGuard {
	private GuideBookGuard() {
	}

	/** true si el clic debe cancelarse. */
	public static boolean blocks(AbstractContainerMenu menu, int slotId, int button, ContainerInput input, Player player) {
		Inventory inv = player.getInventory();
		boolean carried = GuideBook.is(menu.getCarried());
		if (slotId == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE) {
			return carried;   // soltar el cursor fuera de la ventana = tirar el libro
		}
		if (slotId < 0 || slotId >= menu.slots.size()) {
			return false;
		}
		Slot slot = menu.slots.get(slotId);
		boolean own = slot.container == inv;
		boolean onSlot = GuideBook.is(slot.getItem());
		return switch (input) {
			case THROW -> onSlot;
			// Mayus+clic desde otro menu (cofre...) lo mandaria fuera; en el inventario normal solo lo recoloca.
			case QUICK_MOVE -> onSlot && !(menu instanceof InventoryMenu);
			// Tecla numerica sobre un hueco ajeno: intercambiaria el libro de la hotbar con un hueco de otro contenedor.
			case SWAP -> !own && (onSlot || (button >= 0 && button < inv.getContainerSize() && GuideBook.is(inv.getItem(button))));
			// Dejar (o arrastrar) el libro del cursor sobre un hueco que no es del inventario del jugador.
			case PICKUP, QUICK_CRAFT -> carried && !own;
			default -> false;
		};
	}
}
