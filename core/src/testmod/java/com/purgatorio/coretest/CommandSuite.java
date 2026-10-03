package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.item.ColmilloDeCeniza;
import com.purgatorio.core.item.UpgradeData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Los comandos de administracion funcionan para operadores y NO para jugadores normales. */
public final class CommandSuite implements Suite {
	@Override
	public String name() {
		return "comandos";
	}

	@Override
	public void run(Ctx ctx) {
		Ctx.Mock m = ctx.join("cmd-test-a");
		ServerPlayer p = m.player();
		var alma = PurgatorioCore.alma();
		CommandSourceStack op = ctx.server.createCommandSourceStack().withSuppressedOutput();
		CommandSourceStack user = p.createCommandSourceStack().withSuppressedOutput();

		alma.set(p, AlmaRules.fromPoints(10));
		ctx.server.getCommands().performPrefixedCommand(user, "purgatorio admin alma set cmd-test-a 100");
		ctx.eq(AlmaRules.fromPoints(10), alma.getCentis(p), "un jugador normal NO puede usar /purgatorio admin alma set");
		ctx.server.getCommands().performPrefixedCommand(user, "purgatorio admin dar colmillo cmd-test-a 3");
		ctx.check(!ForgeSuite.hasSword(p), "un jugador normal NO puede darse el Colmillo");

		ctx.server.getCommands().performPrefixedCommand(op, "purgatorio admin alma set cmd-test-a 25.5");
		ctx.eq(2550, alma.getCentis(p), "operador: set 25,5");
		ctx.server.getCommands().performPrefixedCommand(op, "purgatorio admin alma add cmd-test-a 10");
		ctx.eq(3550, alma.getCentis(p), "operador: add 10");
		ctx.server.getCommands().performPrefixedCommand(op, "purgatorio admin alma remove cmd-test-a 5");
		ctx.eq(3050, alma.getCentis(p), "operador: remove 5");
		ctx.server.getCommands().performPrefixedCommand(op, "purgatorio admin alma add cmd-test-a 100");
		ctx.eq(AlmaRules.MAX, alma.getCentis(p), "el comando tampoco supera 100");
		ctx.server.getCommands().performPrefixedCommand(op, "purgatorio admin alma set cmd-test-a 150");
		ctx.eq(AlmaRules.MAX, alma.getCentis(p), "un valor fuera de rango (150) se rechaza y no cambia nada");

		ctx.server.getCommands().performPrefixedCommand(op, "purgatorio admin dar colmillo cmd-test-a 2");
		ItemStack given = ForgeSuite.findSword(p);
		ctx.check(given != null, "operador: /dar colmillo entrega el objeto");
		if (given != null) {
			ctx.eq(2, UpgradeData.get(given), "con la mejora pedida (2)");
			ctx.near(ColmilloDeCeniza.damageForLevel(2), ForgeSuite.weaponDamage(given), 1e-9, "y su dano correspondiente");
		}

		// El menu y la forja los puede abrir cualquier jugador.
		ctx.server.getCommands().performPrefixedCommand(user, "purgatorio");
		ctx.check(p.containerMenu != p.inventoryMenu, "cualquier jugador puede abrir /purgatorio");
	}
}
