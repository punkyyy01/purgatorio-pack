package com.purgatorio.core.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaEvents;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.enemy.EnemySpawner;
import com.purgatorio.core.forge.ForgeGui;
import com.purgatorio.core.item.PurgatorioItems;
import com.purgatorio.core.item.UpgradeData;
import com.purgatorio.core.menu.MainMenu;
import java.util.Collection;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * /purgatorio              -> menu
 * /purgatorio forja        -> forja
 * /purgatorio admin ...    -> solo operadores: Alma, objetos y enemigos de prueba
 */
public final class PurgatorioCommands {
	private PurgatorioCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> dispatcher.register(
			Commands.literal("purgatorio")
				.executes(c -> {
					MainMenu.open(c.getSource().getPlayerOrException());
					return 1;
				})
				.then(Commands.literal("forja").executes(c -> {
					ForgeGui.open(c.getSource().getPlayerOrException(), null);
					return 1;
				}))
				.then(Commands.literal("admin")
					.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
					.then(Commands.literal("alma")
						.then(Commands.literal("get")
							.then(Commands.argument("jugador", EntityArgument.player()).executes(PurgatorioCommands::almaGet)))
						.then(Commands.literal("set")
							.then(Commands.argument("jugador", EntityArgument.players())
								.then(Commands.argument("puntos", DoubleArgumentType.doubleArg(0, 100))
									.executes(c -> almaChange(c, Mode.SET)))))
						.then(Commands.literal("add")
							.then(Commands.argument("jugador", EntityArgument.players())
								.then(Commands.argument("puntos", DoubleArgumentType.doubleArg(0, 100))
									.executes(c -> almaChange(c, Mode.ADD)))))
						.then(Commands.literal("remove")
							.then(Commands.argument("jugador", EntityArgument.players())
								.then(Commands.argument("puntos", DoubleArgumentType.doubleArg(0, 100))
									.executes(c -> almaChange(c, Mode.REMOVE))))))
					.then(Commands.literal("dar")
						.then(Commands.literal("colmillo")
							.then(Commands.argument("jugador", EntityArgument.players())
								.executes(PurgatorioCommands::giveColmillo)
								.then(Commands.argument("mejora", IntegerArgumentType.integer(0, 3))
									.executes(PurgatorioCommands::giveColmillo)))))
					.then(Commands.literal("invocar")
						.then(Commands.literal("acechador").executes(c -> {
							ServerPlayer player = c.getSource().getPlayerOrException();
							EnemySpawner.spawnAcechador(player.level(), player.position().add(6, 0, 0));
							return 1;
						}))))
		));
	}

	private enum Mode {
		SET, ADD, REMOVE
	}

	private static int almaGet(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(c, "jugador");
		int centis = PurgatorioCore.alma().getCentis(player);
		c.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " tiene " + AlmaEvents.format(centis) + " de Alma"), false);
		return centis / AlmaRules.SCALE;
	}

	private static int almaChange(CommandContext<CommandSourceStack> c, Mode mode) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		Collection<ServerPlayer> players = EntityArgument.getPlayers(c, "jugador");
		int centis = (int) Math.round(DoubleArgumentType.getDouble(c, "puntos") * AlmaRules.SCALE);
		for (ServerPlayer player : players) {
			switch (mode) {
				case SET -> PurgatorioCore.alma().set(player, centis);
				case ADD -> PurgatorioCore.alma().add(player, centis);
				case REMOVE -> PurgatorioCore.alma().remove(player, centis);
			}
		}
		c.getSource().sendSuccess(() -> Component.literal("Alma " + mode.name().toLowerCase() + " " + AlmaEvents.format(centis) + " para " + players.size() + " jugador(es)"), true);
		return players.size();
	}

	private static int giveColmillo(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		int level;
		try {
			level = IntegerArgumentType.getInteger(c, "mejora");
		} catch (IllegalArgumentException e) {
			level = 0;
		}
		Collection<ServerPlayer> players = EntityArgument.getPlayers(c, "jugador");
		for (ServerPlayer player : players) {
			ItemStack stack = new ItemStack(PurgatorioItems.COLMILLO_DE_CENIZA);
			UpgradeData.set(stack, level);
			PurgatorioItems.COLMILLO_DE_CENIZA.applyLevel(stack, level);
			player.getInventory().add(stack);
		}
		return players.size();
	}
}
