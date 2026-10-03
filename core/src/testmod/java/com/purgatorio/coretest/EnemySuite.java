package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.enemy.AcechadorBehavior;
import com.purgatorio.core.enemy.EnemySpawner;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** El Acechador de Ceniza: control server-side de atributos, botin, comportamiento con aviso y recompensa. */
public final class EnemySuite implements Suite {
	@Override
	public String name() {
		return "enemigo";
	}

	@Override
	public void run(Ctx ctx) {
		Ctx.Mock m = ctx.join("enemigo-test-a");
		ServerPlayer player = m.player();
		var alma = PurgatorioCore.alma();
		alma.set(player, 0);
		player.snapTo(0.5, -60, 0.5, 0.0F, 0.0F);   // mira hacia +Z (rotacion 0)

		int trackedBefore = AcechadorBehavior.trackedCount();
		Husk husk = EnemySpawner.spawnAcechador(ctx.level, new Vec3(0.5, -60, 12.5));
		husk.setNoAi(true);   // el movimiento propio lo desactivamos: solo medimos el teletransporte
		ctx.eq(trackedBefore + 1, AcechadorBehavior.trackedCount(), "el Acechador se registra al aparecer");
		ctx.near(36.0, husk.getMaxHealth(), 1e-6, "vida maxima 36 (un Husk normal tiene 20)");
		ctx.near(0.30, husk.getAttributeValue(Attributes.MOVEMENT_SPEED), 1e-6, "velocidad 0,30 (un Husk normal 0,23)");
		ctx.check(husk.entityTags().contains(AcechadorBehavior.TAG), "lleva la etiqueta de comportamiento");
		ctx.eq("Acechador de Ceniza", husk.getCustomName().getString(), "tiene nombre propio");
		ctx.check(husk.getLootTable().isPresent() && husk.getLootTable().get().identifier().equals(Identifier.fromNamespaceAndPath("purgatorio", "entities/acechador")),
			"su botin sale de la tabla del datapack purgatorio:entities/acechador (real=" + husk.getLootTable() + ")");

		// ---- sin objetivo no hace nada ----
		Vec3 start = husk.position();
		for (int i = 0; i < 300; i++) {
			AcechadorBehavior.tick(ctx.server);
		}
		ctx.check(husk.position().distanceTo(start) < 1e-6, "sin objetivo el Acechador no se teletransporta");

		// ---- con objetivo: AVISO de 1 s y luego aparece a la ESPALDA ----
		husk.setTarget(player);
		for (int i = 0; i < 99; i++) {
			AcechadorBehavior.tick(ctx.server);
		}
		ctx.check(husk.position().distanceTo(start) < 1e-6, "durante la espera inicial no se mueve");
		AcechadorBehavior.tick(ctx.server);               // tick 100: empieza el aviso
		for (int i = 0; i < 19; i++) {
			AcechadorBehavior.tick(ctx.server);           // 19 ticks de aviso: sigue en su sitio
		}
		ctx.check(husk.position().distanceTo(start) < 1e-6, "durante el aviso (telegraph de 20 ticks) sigue donde estaba");
		AcechadorBehavior.tick(ctx.server);               // ticks 120: se teletransporta
		Vec3 now = husk.position();
		ctx.check(now.distanceTo(start) > 5.0, "tras el aviso se teletransporta (se movio " + String.format("%.1f", now.distanceTo(start)) + " bloques)");
		Vec3 look = player.getLookAngle();
		Vec3 rel = now.subtract(player.position());
		ctx.check(rel.x * look.x + rel.z * look.z < -1.0, "aparece DETRAS del jugador (producto escalar con la mirada = " + String.format("%.2f", rel.x * look.x + rel.z * look.z) + ")");
		ctx.near(1.8, Math.hypot(rel.x, rel.z), 0.05, "a ~1,8 bloques de la espalda");

		// ---- si el jugador ya esta pegado, no hace falta teletransportarse ----
		Vec3 near = husk.position();
		for (int i = 0; i < 400; i++) {
			AcechadorBehavior.tick(ctx.server);
		}
		ctx.check(husk.position().distanceTo(near) < 1e-6, "con el jugador a menos de 4 bloques no se teletransporta");

		// ---- la tabla de botin del datapack existe y produce la esquirla ----
		net.minecraft.world.level.storage.loot.LootTable table = ctx.server.reloadableRegistries().getLootTable(EnemySpawner.ACECHADOR_LOOT);
		ctx.check(table != net.minecraft.world.level.storage.loot.LootTable.EMPTY, "la tabla purgatorio:entities/acechador esta cargada (no es la vacia)");
		var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(ctx.level)
			.withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, husk)
			.withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, husk.position())
			.withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.DAMAGE_SOURCE, ctx.level.damageSources().playerAttack(player))
			.create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.ENTITY);
		var rolled = table.getRandomItems(params);
		var shardItem = ForgeSuite.shard();
		ctx.check(shardItem != null && rolled.stream().anyMatch(i -> i.is(shardItem)), "al tirar la tabla sale la Esquirla de Brasa (salio: " + rolled + ")");

		// ---- botin y recompensa de Alma al matarlo ----
		alma.set(player, 0);
		Vec3 deathPos = husk.position();
		husk.setNoAi(false);
		husk.hurtServer(ctx.level, ctx.level.damageSources().playerAttack(player), 1000.0F);
		ctx.check(husk.isDeadOrDying() || husk.isRemoved(), "el Acechador muere");
		List<ItemEntity> drops = ctx.level.getEntitiesOfClass(ItemEntity.class, new AABB(deathPos, deathPos).inflate(6));
		var shard = ForgeSuite.shard();
		int shards = drops.stream().filter(e -> shard != null && e.getItem().is(shard)).mapToInt(e -> e.getItem().getCount()).sum();
		ctx.check(shard != null && shards >= 1 && shards <= 2, "suelta 1-2 Esquirlas de Brasa por su tabla de botin (solto " + shards + ")");
		ctx.eq(AcechadorBehavior.ALMA_REWARD_CENTIS, alma.getCentis(player), "matarlo da 3 de Alma (la recompensa real es la esquirla)");
		ctx.eq(0, ctx.level.getEntitiesOfClass(ExperienceOrb.class, new AABB(deathPos, deathPos).inflate(10)).size(), "no suelta XP vanilla");
		drops.forEach(ItemEntity::discard);

		// ---- un mob que muere por otra causa no da Alma ----
		alma.set(player, 0);
		Husk other = EnemySpawner.spawnAcechador(ctx.level, new Vec3(3.5, -60, 3.5));
		other.hurtServer(ctx.level, ctx.level.damageSources().generic(), 1000.0F);
		ctx.eq(0, alma.getCentis(player), "si no lo mata un jugador no se da Alma");
		ctx.level.getEntitiesOfClass(ItemEntity.class, new AABB(3.5, -62, 3.5, 4.5, -58, 4.5).inflate(6)).forEach(ItemEntity::discard);
		AcechadorBehavior.tick(ctx.server);   // el seguimiento descarta a los muertos en el siguiente tick
		ctx.eq(trackedBefore, AcechadorBehavior.trackedCount(), "los Acechadores muertos dejan de seguirse");
	}
}
