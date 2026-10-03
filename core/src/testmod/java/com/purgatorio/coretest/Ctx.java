package com.purgatorio.coretest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Contexto de una prueba: jugadores simulados reales (con canal de red simulado) y asserts. */
public final class Ctx {
	private static final Logger LOGGER = LoggerFactory.getLogger("purgatorio_core_testmod");

	/** Jugador simulado. La entidad cambia al reaparecer, por eso es mutable. */
	public static final class Mock {
		public final String name;
		public final EmbeddedChannel channel;
		private ServerPlayer player;

		Mock(String name, ServerPlayer player, EmbeddedChannel channel) {
			this.name = name;
			this.player = player;
			this.channel = channel;
		}

		public ServerPlayer player() {
			return player;
		}

		public EmbeddedChannel channel() {
			return channel;
		}

		public void setPlayer(ServerPlayer player) {
			this.player = player;
		}
	}

	public final MinecraftServer server;
	public final ServerLevel level;
	private final List<String> failures = new ArrayList<>();
	private final List<Mock> open = new ArrayList<>();
	private int checks;
	/** true si el ultimo {@link #die} no llego a matar al jugador porque otro mod lo evito (jugador caido). */
	public boolean lastDeathPrevented;

	Ctx(MinecraftServer server) {
		this.server = server;
		this.level = server.overworld();
	}

	/**
	 * Mata al jugador y lo hace reaparecer por el mismo camino que el boton "Reaparecer" del cliente
	 * (ServerboundClientCommandPacket) y confirma la carga. Actualiza el Mock.
	 */
	public ServerPlayer die(Mock mock) {
		ServerPlayer dying = mock.player();
		dying.setHealth(dying.getMaxHealth());
		dying.kill(level);
		lastDeathPrevented = !dying.isDeadOrDying();
		if (lastDeathPrevented) {
			// Otro mod (p. ej. Down But Not Out con mas jugadores conectados) convirtio la muerte en "caido".
			return dying;
		}
		dying.connection.handleClientCommand(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
		ServerPlayer respawned = dying.connection.getPlayer();
		respawned.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
		mock.setPlayer(respawned);
		return respawned;
	}

	/** Mete a un jugador simulado en el servidor exactamente como el login real (carga sus datos guardados). */
	public Mock join(String name) {
		GameProfile profile = new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name);
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
		ServerPlayer player = new ServerPlayer(server, level, profile, cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		EmbeddedChannel channel = new EmbeddedChannel(connection);
		// Igual que PrepareSpawnTask: se cargan los datos guardados ANTES de colocar al jugador.
		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(player.problemPath(), LOGGER)) {
			Optional<ValueInput> saved = server.getPlayerList()
				.loadPlayerData(player.nameAndId())
				.map(tag -> TagValueInput.create(reporter, server.registryAccess(), tag));
			saved.ifPresent(player::load);
			player.snapTo(0.5, 80, 0.5, 0.0F, 0.0F);
			server.getPlayerList().placeNewPlayer(connection, player, cookie);
		}
		// El cliente "termino de cargar": hasta entonces el jugador es invulnerable.
		player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
		Mock mock = new Mock(name, player, channel);
		open.add(mock);
		return mock;
	}

	/** Desconecta guardando los datos, como al salir de verdad. */
	public void leave(Mock mock) {
		server.getPlayerList().remove(mock.player());
		open.remove(mock);
	}

	/** Todos los paquetes de barra de XP enviados a este jugador, en orden. */
	public List<ClientboundSetExperiencePacket> xpPackets(Mock mock) {
		List<ClientboundSetExperiencePacket> out = new ArrayList<>();
		for (Object o : mock.channel().outboundMessages()) {
			if (o instanceof ClientboundSetExperiencePacket p) {
				out.add(p);
			}
		}
		return out;
	}

	public ClientboundSetExperiencePacket lastXp(Mock mock) {
		List<ClientboundSetExperiencePacket> all = xpPackets(mock);
		return all.isEmpty() ? null : all.get(all.size() - 1);
	}

	void cleanup() {
		for (Mock m : new ArrayList<>(open)) {
			try {
				server.getPlayerList().remove(m.player());
			} catch (RuntimeException ignored) {
			}
		}
		open.clear();
	}

	// --- asserts ---
	public void check(boolean condition, String message) {
		checks++;
		if (!condition) {
			failures.add(message);
		}
	}

	public void eq(Object expected, Object actual, String message) {
		check(java.util.Objects.equals(expected, actual), message + " (esperado=" + expected + ", real=" + actual + ")");
	}

	public void near(double expected, double actual, double eps, String message) {
		check(Math.abs(expected - actual) <= eps, message + " (esperado=" + expected + ", real=" + actual + ")");
	}

	List<String> failures() {
		return failures;
	}

	int checks() {
		return checks;
	}
}
