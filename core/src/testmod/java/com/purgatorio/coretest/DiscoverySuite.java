package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.menu.MainMenu;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;

/** Descubrir un lugar por primera vez: Alma, logro en el Diario, recompensa individual por jugador. */
public final class DiscoverySuite implements Suite {
	private static final Identifier RUINA = Identifier.fromNamespaceAndPath("purgatorio", "diario/ruina_de_ceniza");
	private static final Identifier RAIZ = Identifier.fromNamespaceAndPath("purgatorio", "diario/raiz");

	@Override
	public String name() {
		return "descubrimiento";
	}

	private boolean done(Ctx ctx, ServerPlayer p, AdvancementHolder holder) {
		return p.getAdvancements().getOrStartProgress(holder).isDone();
	}

	@Override
	public void run(Ctx ctx) {
		AdvancementHolder ruina = ctx.server.getAdvancements().get(RUINA);
		AdvancementHolder raiz = ctx.server.getAdvancements().get(RAIZ);
		ctx.check(ruina != null, "el logro purgatorio:diario/ruina_de_ceniza esta cargado");
		ctx.check(raiz != null, "el logro raiz del Diario esta cargado");
		if (ruina == null || raiz == null) {
			return;
		}
		var alma = PurgatorioCore.alma();
		Ctx.Mock a = ctx.join("desc-test-a");
		Ctx.Mock b = ctx.join("desc-test-b");
		ServerPlayer pa = a.player();
		ServerPlayer pb = b.player();
		alma.set(pa, 0);
		alma.set(pb, 0);

		// El Diario se abre solo con el primer tick.
		CriteriaTriggers.TICK.trigger(pa);
		ctx.check(done(ctx, pa, raiz), "la raiz del Diario se concede sola");

		// Lugar real: donde el mundo genero la ruina (sin coordenadas fijas).
		BlockPos ruin = RuinHelper.center(ctx.level);
		ctx.check(ruin != null, "el mundo tiene una Ruina de Ceniza generada");
		if (ruin == null) {
			return;
		}
		Vec3 inside = RuinHelper.inside(ctx.level);
		Vec3 outside = new Vec3(ruin.getX() + 60.5, inside.y, ruin.getZ() + 60.5);

		// Lejos de la ruina no pasa nada.
		pa.snapTo(outside.x, outside.y, outside.z, 0, 0);
		CriteriaTriggers.LOCATION.trigger(pa);
		ctx.check(!done(ctx, pa, ruina), "lejos de la ruina no se descubre");
		ctx.eq(0, alma.getCentis(pa), "lejos de la ruina no hay Alma");
		ctx.check(MainMenu.discoveries(pa).isEmpty(), "el Diario no lista lugares que no has visto");

		// Entrar en la ruina.
		pa.snapTo(inside.x, inside.y, inside.z, 0, 0);
		CriteriaTriggers.LOCATION.trigger(pa);
		ctx.check(done(ctx, pa, ruina), "al entrar en la ruina el logro se completa");
		boolean title = false, bell = false, actionbar = false;
		for (Object o : a.channel().outboundMessages()) {
			title |= o instanceof net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
			bell |= o instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket;
			actionbar |= o instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket sc && sc.overlay();
		}
		ctx.check(title, "feedback: aparece el titulo 'La Ruina de Ceniza'");
		ctx.check(bell, "feedback: suena la campana");
		ctx.check(actionbar, "feedback: el texto sobre la barra cuenta lo ganado con voz propia (no un mensaje tecnico)");
		ctx.eq(AlmaRules.fromPoints(8), alma.getCentis(pa), "descubrir la ruina da 8 de Alma");
		ctx.eq(1, MainMenu.discoveries(pa).size(), "el Diario ya lista el descubrimiento");

		// Una sola vez por jugador.
		pa.snapTo(outside.x, outside.y, outside.z, 0, 0);
		pa.snapTo(inside.x, inside.y, inside.z, 0, 0);
		CriteriaTriggers.LOCATION.trigger(pa);
		CriteriaTriggers.LOCATION.trigger(pa);
		ctx.eq(AlmaRules.fromPoints(8), alma.getCentis(pa), "volver a entrar NO repite la recompensa");

		// Individual: B no recibio nada por lo de A.
		ctx.check(!done(ctx, pb, ruina), "B no tiene el logro de A");
		ctx.eq(0, alma.getCentis(pb), "B no recibe Alma por lo que descubre A");
		ctx.check(MainMenu.discoveries(pb).isEmpty(), "el Diario de B sigue vacio");

		// B lo descubre por su cuenta y recibe SU recompensa.
		pb.snapTo(inside.x + 2, inside.y, inside.z + 2, 0, 0);
		CriteriaTriggers.LOCATION.trigger(pb);
		ctx.check(done(ctx, pb, ruina), "B descubre la ruina por su cuenta");
		ctx.eq(AlmaRules.fromPoints(8), alma.getCentis(pb), "B recibe sus 8 de Alma");
		ctx.eq(AlmaRules.fromPoints(8), alma.getCentis(pa), "A sigue con 8 (no se duplica)");

		// Persistencia del logro y del Alma.
		ctx.leave(a);
		Ctx.Mock a2 = ctx.join("desc-test-a");
		ctx.check(done(ctx, a2.player(), ruina), "el logro persiste tras reconectar");
		a2.player().snapTo(inside.x, inside.y, inside.z, 0, 0);
		CriteriaTriggers.LOCATION.trigger(a2.player());
		ctx.eq(AlmaRules.fromPoints(8), alma.getCentis(a2.player()), "tras reconectar sigue sin repetirse la recompensa");
	}
}
