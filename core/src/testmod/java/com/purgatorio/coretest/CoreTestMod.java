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
import net.minecraft.server.level.ServerLevel;
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
		all.add(new ForgeSuite());
		all.add(new ItemSuite());
		all.add(new DiscoverySuite());
		all.add(new EnemySuite());
		all.add(new MenuSuite());
		all.add(new CommandSuite());
		all.add(new XpCompatSuite());
		all.add(new RuinaSuite());
		all.add(new FeedbackSuite());
		return all;
	}

	@Override
	public void onInitialize() {
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (ticksUntilRun > 0 && --ticksUntilRun == 0) {
				try {
					runNow(server, pendingFilter);
				} finally {
					ServerLevel level = server.overworld();
					for (int cx = -1; cx <= 3; cx++) {
						for (int cz = -1; cz <= 3; cz++) {
							level.setChunkForced(cx, cz, false);
						}
					}
					net.minecraft.world.level.ChunkPos rc = RuinHelper.chunk(level);
					for (int cx = rc.x() - 2; cx <= rc.x() + 2; cx++) {
						for (int cz = rc.z() - 2; cz <= rc.z() + 2; cz++) {
							level.setChunkForced(cx, cz, false);
						}
					}
				}
			}
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) ->
			dispatcher.register(Commands.literal("purgatorio_test")
				.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("run")
					.executes(c -> run(c.getSource(), ""))
					.then(Commands.argument("filtro", StringArgumentType.word())
						.executes(c -> run(c.getSource(), StringArgumentType.getString(c, "filtro")))))));
	}

	/**
	 * Programa la ejecucion como una tarea normal del servidor: asi las funciones y comandos que lanzan
	 * las pruebas corren de inmediato (dentro de un comando se encolarian hasta que este termine).
	 */
	private static int run(CommandSourceStack source, String filter) {
		MinecraftServer server = source.getServer();
		try {
			Files.deleteIfExists(Path.of("purgatorio-test-results.txt"));
		} catch (IOException ignored) {
		}
		// Zona de pruebas (incluye la ruina en x,z 36..45): se fuerzan los chunks y se esperan ticks REALES
		// para que pasen a procesar entidades; si no, entidades y botin no se anaden en chunks recien creados.
		ServerLevel level = server.overworld();
		for (int cx = -1; cx <= 3; cx++) {
			for (int cz = -1; cz <= 3; cz++) {
				level.setChunkForced(cx, cz, true);
			}
		}
		// Zona de la ruina: hay que generarla con ticks reales para que sus entidades existan.
		net.minecraft.world.level.ChunkPos rc = RuinHelper.chunk(level);
		for (int cx = rc.x() - 2; cx <= rc.x() + 2; cx++) {
			for (int cz = rc.z() - 2; cz <= rc.z() + 2; cz++) {
				level.setChunkForced(cx, cz, true);
			}
		}
		pendingFilter = filter;
		ticksUntilRun = 100;
		source.sendSuccess(() -> Component.literal("Pruebas programadas (en 60 ticks); resultados en purgatorio-test-results.txt"), false);
		return 1;
	}

	private static String pendingFilter = "";
	private static int ticksUntilRun = -1;

	private static void runNow(MinecraftServer server, String filter) {
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
			Path tmp = Path.of("purgatorio-test-results.tmp");
			Files.writeString(tmp, report.toString(), StandardCharsets.UTF_8);
			Files.move(tmp, Path.of("purgatorio-test-results.txt"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("No se pudo escribir el informe", e);
		}
		LOGGER.info("\n{}", report);
		LOGGER.info("RESUMEN: {} ok, {} con fallos", passed, failed);
	}
}
