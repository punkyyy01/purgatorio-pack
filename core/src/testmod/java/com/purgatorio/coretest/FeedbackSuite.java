package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.enemy.AcechadorBehavior;
import com.purgatorio.core.enemy.EnemySpawner;
import com.purgatorio.core.feedback.AlmaReason;
import com.purgatorio.core.forge.ForgeService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/** P2: el juego habla con voz propia (sonido, titulos, texto sobre la barra) en los momentos que importan. */
public final class FeedbackSuite implements Suite {
	@Override
	public String name() {
		return "feedback";
	}

	/** Todo lo que el jugador "oyo" y "vio" desde la ultima limpieza. */
	private static final class Seen {
		final List<String> overlay = new ArrayList<>();
		final List<String> titles = new ArrayList<>();
		final List<String> chat = new ArrayList<>();
		int sounds, particles;

		static Seen of(Ctx.Mock m) {
			Seen s = new Seen();
			for (Object o : m.channel().outboundMessages()) {
				if (o instanceof ClientboundSystemChatPacket sc) {
					(sc.overlay() ? s.overlay : s.chat).add(sc.content().getString());
				} else if (o instanceof net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket ab) {
					s.overlay.add(ab.text().getString());      // /title actionbar de las funciones del datapack
				} else if (o instanceof ClientboundSetTitleTextPacket t) {
					s.titles.add(t.text().getString());
				} else if (o instanceof ClientboundSoundPacket) {
					s.sounds++;
				} else if (o instanceof ClientboundLevelParticlesPacket) {
					s.particles++;
				}
			}
			return s;
		}

		boolean overlayHas(String text) {
			return overlay.stream().anyMatch(x -> x.contains(text));
		}

		boolean titleHas(String text) {
			return titles.stream().anyMatch(x -> x.contains(text));
		}
	}

	private static void clear(Ctx.Mock m) {
		m.channel().outboundMessages().clear();
	}

	private boolean done(ServerPlayer p, Ctx ctx, String path) {
		AdvancementHolder h = ctx.server.getAdvancements().get(Identifier.fromNamespaceAndPath("purgatorio", "diario/" + path));
		return h != null && p.getAdvancements().getOrStartProgress(h).isDone();
	}

	@Override
	public void run(Ctx ctx) {
		var alma = PurgatorioCore.alma();
		Ctx.Mock m = ctx.join("fx-test-a");
		ServerPlayer p = m.player();
		p.snapTo(1.5, -60, 1.5, 0, 0);
		alma.set(p, 0);

		// ---- ganar Alma: voz segun el motivo; en silencio no hay nada ----
		clear(m);
		alma.add(p, AlmaRules.fromPoints(5), AlmaReason.SILENT);
		Seen s = Seen.of(m);
		ctx.check(s.overlay.isEmpty() && s.sounds == 0 && s.particles == 0, "una suma SILENCIOSA (pruebas/administracion) no genera feedback");
		clear(m);
		alma.add(p, AlmaRules.fromPoints(8), AlmaReason.DESCUBRIMIENTO);
		s = Seen.of(m);
		ctx.check(s.overlayHas("El lugar te reconoce"), "descubrir: 'El lugar te reconoce' (no un '+8' a secas): " + s.overlay);
		ctx.check(s.overlayHas("+8 de Alma"), "y aun asi dice cuanto");
		ctx.check(s.sounds >= 1 && s.particles >= 1, "con sonido y particulas de almas");
		clear(m);
		alma.add(p, AlmaRules.fromPoints(3), AlmaReason.DERROTA);
		s = Seen.of(m);
		ctx.check(s.overlayHas("Algo de lo que fue se queda contigo"), "derrotar: tiene su propia voz: " + s.overlay);
		clear(m);
		alma.set(p, AlmaRules.MAX);
		clear(m);
		alma.add(p, AlmaRules.fromPoints(5), AlmaReason.DERROTA);
		ctx.check(Seen.of(m).overlay.isEmpty(), "con el Alma al maximo no se anuncia una ganancia que no ocurrio");

		// ---- perder Alma: el momento de la muerte ----
		alma.set(p, AlmaRules.fromPoints(100));
		clear(m);
		ServerPlayer revived = ctx.die(m);
		s = Seen.of(m);
		ctx.check(s.titleHas("Tu Alma se derrama"), "morir: titulo 'Tu Alma se derrama': " + s.titles);
		ctx.check(s.sounds >= 1 && s.particles >= 1, "con sonido y particulas");
		ctx.eq(AlmaRules.fromPoints(70), alma.getCentis(revived), "(y el 30 % sigue siendo el 30 %)");
		p = revived;

		// ---- gastar Alma en la forja ----
		alma.set(p, AlmaRules.fromPoints(50));
		ForgeSuite.clear(p);
		ItemStack sword = ForgeSuite.giveSword(p, 0);
		p.getInventory().add(new ItemStack(Items.IRON_INGOT, 4));
		clear(m);
		ForgeService.Outcome out = PurgatorioCore.forgeService().tryUpgrade(p, sword);
		ctx.check(out.ok(), "(la mejora se aplica)");
		s = Seen.of(m);
		ctx.check(s.overlayHas("La mejora arde en el metal"), "forjar: 'La mejora arde en el metal': " + s.overlay);
		ctx.check(s.sounds >= 2, "con yunque y fuego (" + s.sounds + " sonidos)");

		// ---- encontrar un objeto especial: el Colmillo y la Esquirla (logros por inventario) ----
		ForgeSuite.clear(p);
		ctx.check(!done(p, ctx, "colmillo") && !done(p, ctx, "esquirla"), "(antes de tenerlos, el Diario no los lista)");
		clear(m);
		p.getInventory().add(new ItemStack(com.purgatorio.core.item.PurgatorioItems.COLMILLO_DE_CENIZA));
		p.inventoryMenu.broadcastChanges();
		ctx.check(done(p, ctx, "colmillo"), "tener el Colmillo concede su entrada del Diario");
		s = Seen.of(m);
		ctx.check(s.overlayHas("todavía arde") || s.overlayHas("todavía arde"), "encontrar el Colmillo: 'Algo en este colmillo todavia arde': " + s.overlay);
		clear(m);
		var shard = ForgeSuite.shard();
		p.getInventory().add(new ItemStack(shard, 2));
		p.inventoryMenu.broadcastChanges();
		ctx.check(done(p, ctx, "esquirla"), "tener una Esquirla concede su entrada");
		s = Seen.of(m);
		ctx.check(s.overlayHas("tibio") && s.sounds >= 1, "encontrar la Esquirla: 'Un fragmento todavia tibio' con sonido: " + s.overlay);
		clear(m);
		p.getInventory().add(new ItemStack(shard, 1));
		p.inventoryMenu.broadcastChanges();
		ctx.check(Seen.of(m).overlay.isEmpty(), "la segunda vez no se repite (solo la primera es especial)");

		// ---- encontrarse con un enemigo: el aviso con voz propia ----
		p.snapTo(1.5, -60, 1.5, 0.0F, 0.0F);
		Husk husk = EnemySpawner.spawnAcechador(ctx.level, new Vec3(1.5, -60, 14.5));
		husk.setNoAi(true);
		husk.setTarget(p);
		clear(m);
		for (int i = 0; i < 101; i++) {
			AcechadorBehavior.tick(ctx.server);
		}
		s = Seen.of(m);
		ctx.check(s.overlayHas("presencia a tu espalda"), "el aviso del Acechador te lo dice: 'Sientes una presencia a tu espalda...': " + s.overlay);
		ctx.check(s.sounds >= 1, "y suena");

		// ---- derrotar algo importante ----
		alma.set(p, 0);
		clear(m);
		ctx.check(!done(p, ctx, "acechador"), "(antes: sin la entrada 'Ceniza al viento')");
		husk.hurtServer(ctx.level, ctx.level.damageSources().playerAttack(p), 1000.0F);
		s = Seen.of(m);
		ctx.check(done(p, ctx, "acechador"), "derrotar al Acechador concede 'Ceniza al viento'");
		ctx.check(s.titleHas("Ceniza al viento"), "con titulo propio: " + s.titles);
		ctx.check(s.overlayHas("Algo de lo que fue se queda contigo"), "y el Alma suena a victoria, no a contador: " + s.overlay);
		ctx.eq(AcechadorBehavior.ALMA_REWARD_CENTIS, alma.getCentis(p), "(+3 de Alma)");
		Husk second = EnemySpawner.spawnAcechador(ctx.level, new Vec3(3.5, -60, 14.5));
		clear(m);
		second.hurtServer(ctx.level, ctx.level.damageSources().playerAttack(p), 1000.0F);
		s = Seen.of(m);
		ctx.check(!s.titleHas("Ceniza al viento"), "el titulo solo sale la primera vez");
		ctx.check(s.overlayHas("Algo de lo que fue se queda contigo"), "pero cada victoria sigue dando su Alma con voz");
		ctx.check(Component.literal("x") != null, "(fin)");
	}
}
