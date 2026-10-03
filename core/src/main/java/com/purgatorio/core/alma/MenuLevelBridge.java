package com.purgatorio.core.alma;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.EnchantmentMenu;

/**
 * Mantiene funcionando la mesa de encantamientos y el yunque sin devolver el XP vanilla.
 *
 * <p>El cliente ejecuta por su cuenta {@code EnchantmentMenu.clickMenuButton} y {@code Slot.mayPickup} del yunque con SU
 * nivel (el que mostramos como Alma) y solo envia el clic si pasan, asi que no basta con cambiar el servidor. Mientras el
 * jugador tiene abierto uno de esos dos menus se le da un nivel temporal suficiente (tanto en el servidor como en lo que
 * ve el cliente); al cerrarlo todo vuelve a 0 y la barra a su Alma. Efecto: encantar cuesta solo lapis y el yunque no
 * cuesta niveles. El nivel temporal nunca es Alma y nunca se conserva.
 */
public final class MenuLevelBridge {
	/** Suficiente para el requisito maximo de la mesa (30) y para cualquier coste de yunque permitido (< 40). */
	public static final int MENU_LEVEL = 39;

	private MenuLevelBridge() {
	}

	/** El jugador tiene abierto un menu vanilla que comprueba niveles de XP. */
	public static boolean inLevelMenu(ServerPlayer player) {
		return player.containerMenu instanceof EnchantmentMenu || player.containerMenu instanceof AnvilMenu;
	}

	/** Llamar al principio de cada tick del jugador (antes de que vanilla envie la barra). */
	public static void tick(ServerPlayer player) {
		if (inLevelMenu(player)) {
			if (player.experienceLevel != MENU_LEVEL) {
				player.setExperienceLevels(MENU_LEVEL);     // fuerza el reenvio de la barra en este mismo tick
			}
		} else if (player.experienceLevel != 0 || player.totalExperience != 0 || player.experienceProgress != 0.0F) {
			player.setExperienceLevels(0);
			player.setExperiencePoints(0);
			player.totalExperience = 0;
		}
	}
}
