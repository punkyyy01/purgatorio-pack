package com.purgatorio.core.enemy;

import com.purgatorio.core.PurgatorioCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/** Crea enemigos propios a partir de un mob vanilla (atributos, botin por datapack y etiqueta de comportamiento). */
public final class EnemySpawner {
	public static final ResourceKey<LootTable> ACECHADOR_LOOT = ResourceKey.create(Registries.LOOT_TABLE, PurgatorioCore.id("entities/acechador"));

	private EnemySpawner() {
	}

	public static Husk spawnAcechador(ServerLevel level, Vec3 pos) {
		Husk husk = EntityTypes.HUSK.create(level, EntitySpawnReason.COMMAND);
		// El botin sale de un datapack: se asigna por el mismo campo que usa vanilla al cargar la entidad.
		CompoundTag extra = new CompoundTag();
		extra.putString("DeathLootTable", ACECHADOR_LOOT.identifier().toString());
		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(husk.problemPath(), PurgatorioCore.LOGGER)) {
			husk.load(TagValueInput.create(reporter, level.registryAccess(), extra));
		}
		husk.setPos(pos.x, pos.y, pos.z);
		husk.addTag(AcechadorBehavior.TAG);
		husk.setCustomName(Component.literal("Acechador de Ceniza"));
		husk.setCustomNameVisible(true);
		husk.setPersistenceRequired();
		husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(36.0);
		husk.setHealth(36.0F);
		husk.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.30);
		husk.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0);
		if (level.addFreshEntity(husk)) {
			AcechadorBehavior.track(husk);
		}
		return husk;
	}
}
