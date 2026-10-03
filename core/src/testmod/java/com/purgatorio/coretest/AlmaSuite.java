package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.alma.AlmaService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Pruebas del sistema de Alma contra el servidor real (jugadores simulados con canal de red). */
public final class AlmaSuite implements Suite {
	private static final double EPS = 1e-3;

	@Override
	public String name() {
		return "alma";
	}

	private static AlmaService alma() {
		return PurgatorioCore.alma();
	}

	private Zombie zombie(Ctx ctx) {
		Zombie z = EntityTypes.ZOMBIE.create(ctx.level, EntitySpawnReason.COMMAND);
		z.setPos(0.5, 80, 0.5);
		z.setNoAi(true);
		z.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
		ctx.level.addFreshEntity(z);
		z.setHealth(z.getMaxHealth());
		z.setInvulnerableTime(0);
		return z;
	}

	/** Dano real recibido por un zombi sin armadura al ser golpeado por el jugador con 10 de dano base. */
	private float damageDealt(Ctx ctx, ServerPlayer player, boolean viaArrow) {
		float dealt = 0.0F;
		// Con el pack completo algun mod de produccion puede anular un golpe al azar: se reintenta si no hubo dano.
		for (int attempt = 0; attempt < 6 && dealt <= 0.0F; attempt++) {
			Zombie z = zombie(ctx);
			float before = z.getHealth();
			DamageSource source;
			if (viaArrow) {
				Arrow arrow = EntityTypes.ARROW.create(ctx.level, EntitySpawnReason.COMMAND);
				arrow.setOwner(player);
				source = ctx.level.damageSources().arrow(arrow, player);
			} else {
				source = ctx.level.damageSources().playerAttack(player);
			}
			z.hurtServer(ctx.level, source, 10.0F);
			dealt = before - z.getHealth();
			z.discard();
		}
		return dealt;
	}

	@Override
	public void run(Ctx ctx) {
		Ctx.Mock a = ctx.join("alma-test-a");
		ServerPlayer pa = a.player();

		// ---- Alma 0 / 25 / 50 / 75 / 100: dano lineal, +0% .. +10% ----
		int[] points = {0, 25, 50, 75, 100};
		double[] expectedMult = {1.0, 1.025, 1.05, 1.075, 1.10};
		for (int i = 0; i < points.length; i++) {
			alma().set(pa, AlmaRules.fromPoints(points[i]));
			ctx.near(10.0 * expectedMult[i], damageDealt(ctx, pa, false), EPS, "dano cuerpo a cuerpo con Alma " + points[i]);
			ctx.near(10.0 * expectedMult[i], damageDealt(ctx, pa, true), EPS, "dano por flecha (causante=jugador) con Alma " + points[i]);
		}

		// ---- el bonus no se aplica a otros causantes ----
		alma().set(pa, AlmaRules.MAX);
		Zombie attacker = zombie(ctx);
		float hpBefore = pa.getHealth();
		pa.damageCooldownTime = 0;
		pa.hurtServer(ctx.level, ctx.level.damageSources().mobAttack(attacker), 4.0F);
		ctx.near(4.0, hpBefore - pa.getHealth(), EPS, "un mob que golpea al jugador NO recibe bonus (el jugador tiene Alma 100)");
		attacker.discard();
		pa.setHealth(pa.getMaxHealth());
		pa.damageCooldownTime = 0;
		hpBefore = pa.getHealth();
		boolean hurt = pa.hurtServer(ctx.level, ctx.level.damageSources().generic(), 4.0F);
		ctx.check(hurt, "hurtServer(generic) devolvio false: invulnerable=" + pa.isInvulnerableTo(ctx.level, ctx.level.damageSources().generic()) + " invTime=" + pa.getInvulnerableTime() + " hp=" + pa.getHealth() + " clienteCargado=" + pa.connection.hasClientLoaded());
		ctx.near(4.0, hpBefore - pa.getHealth(), EPS, "el dano ambiental (sin causante) NO recibe bonus aunque el jugador tenga Alma 100");
		pa.setHealth(pa.getMaxHealth());

		// ---- tope absoluto: nunca mas de 100 ni mas de +10% ----
		alma().set(pa, 1_000_000);
		ctx.eq(AlmaRules.MAX, alma().getCentis(pa), "set(1.000.000) satura a 100");
		ctx.eq(0, alma().add(pa, 500), "add con Alma 100 no aplica nada");
		ctx.near(11.0, damageDealt(ctx, pa, false), EPS, "con Alma saturada el bonus es exactamente +10%");
		ctx.check(alma().damageMultiplier(pa) <= 1.10 + 1e-9, "damageMultiplier <= 1.10");
		alma().set(pa, -50);
		ctx.eq(0, alma().getCentis(pa), "set(negativo) satura a 0");
		ctx.eq(0, alma().remove(pa, 10_000), "remove con Alma 0 no quita nada");

		// ---- ganar ----
		alma().set(pa, AlmaRules.fromPoints(10));
		ctx.eq(AlmaRules.fromPoints(5), alma().add(pa, AlmaRules.fromPoints(5)), "add devuelve lo aplicado");
		ctx.eq(AlmaRules.fromPoints(15), alma().getCentis(pa), "10 + 5 = 15");
		alma().set(pa, AlmaRules.fromPoints(98));
		ctx.eq(AlmaRules.fromPoints(2), alma().add(pa, AlmaRules.fromPoints(50)), "add cerca del tope solo aplica lo que cabe");
		ctx.eq(AlmaRules.MAX, alma().getCentis(pa), "98 + 50 = 100 (tope)");
		ctx.eq(0, alma().add(pa, -5), "add con valor negativo no hace nada");

		// ---- gastar: reduce el bonus ----
		alma().set(pa, AlmaRules.fromPoints(80));
		ctx.near(10.8, damageDealt(ctx, pa, false), EPS, "Alma 80 = +8%");
		ctx.check(alma().spend(pa, AlmaRules.fromPoints(30)), "gastar 30 teniendo 80 funciona");
		ctx.eq(AlmaRules.fromPoints(50), alma().getCentis(pa), "80 - 30 = 50");
		ctx.near(10.5, damageDealt(ctx, pa, false), EPS, "tras gastar 30 el bonus baja a +5%");
		ctx.check(!alma().spend(pa, AlmaRules.fromPoints(60)), "gastar 60 teniendo 50 falla");
		ctx.eq(AlmaRules.fromPoints(50), alma().getCentis(pa), "un gasto fallido no cambia nada");
		ctx.check(!alma().spend(pa, -1), "gasto negativo rechazado");
		ctx.check(alma().spend(pa, AlmaRules.fromPoints(50)), "gastar todo (50 de 50) funciona");
		ctx.eq(0, alma().getCentis(pa), "queda en 0");

		// ---- barra de XP: sustitucion real de vanilla ----
		alma().set(pa, AlmaRules.fromPoints(42) + 50);
		ClientboundSetExperiencePacket sent = ctx.lastXp(a);
		ctx.check(sent != null, "se envio un paquete de barra de XP al cambiar el Alma");
		if (sent != null) {
			ctx.eq(42, sent.getExperienceLevel(), "la barra muestra nivel = parte entera del Alma (42)");
			ctx.near(0.5, sent.getExperienceProgress(), 1e-6, "la barra muestra progreso = fraccion (0,5)");
		}
		pa.setExperienceLevels(7);     // fuerza el reenvio vanilla (lastSentExp = -1)
		pa.doTick();
		sent = ctx.lastXp(a);
		ctx.check(sent != null && sent.getExperienceLevel() == 42, "el reenvio de vanilla muestra Alma (42) y no el XP real (7)");
		pa.setExperienceLevels(0);
		alma().set(pa, AlmaRules.MAX);
		sent = ctx.lastXp(a);
		ctx.check(sent != null && sent.getExperienceLevel() == 100 && sent.getExperienceProgress() == 1.0F, "con Alma 100 la barra va llena (nivel 100)");

		// ---- cambio de dimension: vanilla reenvia la barra, debe seguir mostrando Alma ----
		alma().set(pa, AlmaRules.fromPoints(33));
		net.minecraft.server.level.ServerLevel nether = ctx.server.getLevel(net.minecraft.world.level.Level.NETHER);
		ctx.check(nether != null, "existe el Nether en el mundo de pruebas");
		if (nether != null) {
			ServerPlayer moved = pa.teleport(new net.minecraft.world.level.portal.TeleportTransition(
				nether, new Vec3(0.5, 70, 0.5), Vec3.ZERO, 0.0F, 0.0F, net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
			ctx.check(moved != null && moved.level() == nether, "el jugador cambio de dimension (Nether)");
			if (moved != null) {
				moved.doTick();
				ClientboundSetExperiencePacket afterPortal = ctx.lastXp(a);
				ctx.check(afterPortal != null && afterPortal.getExperienceLevel() == 33, "tras cambiar de dimension la barra sigue mostrando Alma (33), real="
					+ (afterPortal == null ? "sin paquete" : afterPortal.getExperienceLevel()));
				ctx.eq(AlmaRules.fromPoints(33), alma().getCentis(moved), "y el Alma no cambia por viajar");
				pa = moved;
				a.setPlayer(moved);
				ServerPlayer back = moved.teleport(new net.minecraft.world.level.portal.TeleportTransition(
					ctx.level, new Vec3(0.5, 80, 0.5), Vec3.ZERO, 0.0F, 0.0F, net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
				if (back != null) {
					pa = back;
					a.setPlayer(back);
				}
				pa.hasChangedDimension();    // el cliente confirma el cambio (sin esto el jugador queda invulnerable)
			}
		}

		// ---- el XP vanilla no genera Alma ----
		alma().set(pa, AlmaRules.fromPoints(40));
		pa.experienceLevel = 0;
		pa.totalExperience = 0;
		pa.experienceProgress = 0;
		pa.giveExperiencePoints(1000);
		pa.giveExperienceLevels(10);
		ctx.eq(AlmaRules.fromPoints(40), alma().getCentis(pa), "giveExperiencePoints/Levels no cambian el Alma");
		ctx.eq(0, pa.experienceLevel, "el XP vanilla real no sube (se anulan las ganancias)");
		ctx.eq(0, pa.totalExperience, "totalExperience vanilla sigue en 0");
		ExperienceOrb.award(ctx.level, new Vec3(0.5, 80, 0.5), 500);
		ExperienceOrb.awardWithDirection(ctx.level, new Vec3(0.5, 80, 0.5), new Vec3(0, 1, 0), 500);
		int orbs = ctx.level.getEntitiesOfClass(ExperienceOrb.class, new AABB(BlockPos.containing(0.5, 80, 0.5)).inflate(20)).size();
		ctx.eq(0, orbs, "ExperienceOrb.award* no crea orbes");
		ExperienceOrb orb = new ExperienceOrb(ctx.level, pa.getX(), pa.getY(), pa.getZ(), 300);
		ctx.level.addFreshEntity(orb);
		orb.playerTouch(pa);           // una orbe que exista igualmente no debe dar nada
		ctx.eq(AlmaRules.fromPoints(40), alma().getCentis(pa), "recoger una orbe existente no da Alma");
		ctx.eq(0, pa.experienceLevel, "recoger una orbe no sube el XP real");
		orb.discard();

		// ---- morir: -30% del Alma actual ----
		alma().set(pa, AlmaRules.MAX);
		pa = ctx.die(a);
		ctx.eq(AlmaRules.fromPoints(70), alma().getCentis(pa), "morir con 100 -> 70");
		sentAfterRespawn(ctx, a, 70);
		pa = ctx.die(a);
		ctx.eq(AlmaRules.fromPoints(49), alma().getCentis(pa), "morir con 70 -> 49");
		alma().set(pa, AlmaRules.fromPoints(50));
		pa = ctx.die(a);
		ctx.eq(AlmaRules.fromPoints(35), alma().getCentis(pa), "morir con 50 -> 35");
		alma().set(pa, 0);
		pa = ctx.die(a);
		ctx.eq(0, alma().getCentis(pa), "morir con 0 -> 0");
		ctx.eq(0, pa.experienceLevel, "morir no toca el XP vanilla (siempre 0)");

		// ---- persistencia: salir y volver a entrar ----
		alma().set(pa, 6350);            // 63,5
		ctx.leave(a);
		Ctx.Mock a2 = ctx.join("alma-test-a");
		ctx.eq(6350, alma().getCentis(a2.player()), "al volver a entrar el Alma se conserva (63,5)");
		ClientboundSetExperiencePacket login = ctx.lastXp(a2);
		ctx.check(login != null && login.getExperienceLevel() == 63, "al entrar la barra muestra el Alma guardada");
		ctx.leave(a2);

		// ---- varios jugadores: el Alma es individual ----
		Ctx.Mock ma = ctx.join("alma-test-a");
		Ctx.Mock mb = ctx.join("alma-test-b");
		alma().set(ma.player(), AlmaRules.fromPoints(80));
		alma().set(mb.player(), AlmaRules.fromPoints(20));
		alma().add(ma.player(), AlmaRules.fromPoints(10));
		ctx.eq(AlmaRules.fromPoints(90), alma().getCentis(ma.player()), "A: 80 + 10 = 90");
		ctx.eq(AlmaRules.fromPoints(20), alma().getCentis(mb.player()), "B no cambia cuando A gana Alma");
		alma().spend(ma.player(), AlmaRules.fromPoints(40));
		ctx.eq(AlmaRules.fromPoints(20), alma().getCentis(mb.player()), "B no cambia cuando A gasta Alma");
		ServerPlayer pa2 = ctx.die(ma);
		// Con Down But Not Out (y 2 jugadores conectados) un golpe letal deja al jugador CAIDO, no muerto:
		// no hay muerte, asi que no se pierde Alma (revivir no cuesta Alma). Sin ese mod muere: 50 -> 35.
		int expectedA = ctx.lastDeathPrevented ? AlmaRules.fromPoints(50) : AlmaRules.fromPoints(35);
		ctx.eq(ctx.lastDeathPrevented, net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("down_but_not_out"),
			"la muerte solo se evita si esta Down But Not Out instalado");
		ctx.eq(expectedA, alma().getCentis(pa2), ctx.lastDeathPrevented ? "A caido (DBNO): sin muerte no se pierde Alma (50)" : "A: 50 -> 35 al morir");
		ctx.eq(AlmaRules.fromPoints(20), alma().getCentis(mb.player()), "B no cambia cuando A muere");
		ctx.near(10.0 * 1.02, damageDealt(ctx, mb.player(), false), EPS, "B pega con SU bonus (+2%), no el de A");
		ctx.near(10.0 * (1.0 + expectedA / 100000.0), damageDealt(ctx, pa2, false), EPS, "A pega con SU bonus");
		ctx.check(ctx.lastXp(ma).getExperienceLevel() == expectedA / 100 && ctx.lastXp(mb).getExperienceLevel() == 20, "cada jugador recibe SU barra");
		ctx.leave(ma);
		ctx.leave(mb);
		Ctx.Mock ra = ctx.join("alma-test-a");
		Ctx.Mock rb = ctx.join("alma-test-b");
		ctx.eq(expectedA, alma().getCentis(ra.player()), "A persiste su Alma tras reconectar");
		ctx.eq(AlmaRules.fromPoints(20), alma().getCentis(rb.player()), "B persiste su Alma (20) tras reconectar");
	}

	private void sentAfterRespawn(Ctx ctx, Ctx.Mock mock, int expectedLevel) {
		ClientboundSetExperiencePacket p = ctx.lastXp(mock);
		ctx.check(p != null && p.getExperienceLevel() == expectedLevel, "tras reaparecer la barra muestra " + expectedLevel + " (real=" + (p == null ? "sin paquete" : p.getExperienceLevel()) + ")");
	}
}
