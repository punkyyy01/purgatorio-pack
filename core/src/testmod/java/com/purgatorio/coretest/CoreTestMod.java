package com.purgatorio.coretest;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * /purgatorio_test run [filtro]  -> ejecuta las suites y escribe purgatorio-test-results.txt en el
 * directorio del servidor. Solo existe en el servidor de pruebas.
 */
public final class CoreTestMod implements ModInitializer {
	static final Logger LOGGER = LoggerFactory.getLogger("purgatorio_core_testmod");

	static List<Suite> suites() {
		List<Suite> all = new ArrayList<>();
		all.add(new AlmaSuite());
		return all;
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
			dispatcher.register(Commands.literal("purgatorio_test")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("run")
					.executes(c -> run(c.getSource(), ""))
					.then(Commands.argument("filtro", StringArgumentType.word())
						.executes(c -> run(c.getSource(), StringArgumentType.getString(c, "filtro")))))));
	}

	private static int run(CommandSourceStack source, String filter) {
		MinecraftServer server = source.getServer();
		StringBuilder report = new StringBuilder();
		int passed = 0;
		int failed = 0;
		for (Suite suite : suites()) {
			if (!filter.isEmpty() && !suite.name().contains(filter)) {
				continue;
			}
			Ctx ctx = new Ctx(server);
			String status;
			try {
				suite.run(ctx);
				status = ctx.failures().isEmpty() ? "OK" : "FALLA";
			} catch (Throwable t) {
				ctx.failures().add("EXCEPCION: " + t);
				LOGGER.error("Excepcion en la prueba {}", suite.name(), t);
				status = "FALLA";
			} finally {
				ctx.cleanup();
			}
			if (status.equals("OK")) {
				passed++;
			} else {
				failed++;
			}
			report.append(status).append("  ").append(suite.name()).append("  (").append(ctx.checks()).append(" comprobaciones)\n");
			for (String f : ctx.failures()) {
				report.append("      - ").append(f).append('\n');
			}
		}
		report.append("RESUMEN: ").append(passed).append(" ok, ").append(failed).append(" con fallos\n");
		try {
			Files.writeString(Path.of("purgatorio-test-results.txt"), report.toString(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOGGER.error("No se pudo escribir el informe", e);
		}
		LOGGER.info("\n{}", report);
		String summary = passed + " ok, " + failed + " con fallos (ver purgatorio-test-results.txt)";
		source.sendSuccess(() -> Component.literal(summary), false);
		return failed == 0 ? 1 : 0;
	}
}
