package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.enemy.AcechadorBehavior;
import com.purgatorio.core.feedback.PlaceAmbience;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** P1: La Ruina de Ceniza existe en el mundo normal (generacion natural) y tiene lo que promete. */
public final class RuinaSuite implements Suite {
	@Override
	public String name() {
		return "ruina";
	}

	@Override
	public void run(Ctx ctx) {
		var level = ctx.level;
		ctx.check(RuinHelper.structure(level) != null, "la estructura purgatorio:ruina_de_ceniza esta registrada (datapack)");
		StructureStart start = RuinHelper.start(level);
		ctx.check(start != null, "el mundo GENERO la ruina por si solo (sin comandos) en el chunk " + RuinHelper.chunk(level));
		if (start == null) {
			return;
		}
		var box = start.getBoundingBox();
		ctx.eq(1, start.getPieces().size(), "la ruina es una sola pieza");
		var pieceBox = start.getPieces().get(0).getBoundingBox();
		ctx.check(pieceBox.getXSpan() == 17 && pieceBox.getZSpan() == 17, "la pieza ocupa 17x17 (" + pieceBox.getXSpan() + "x" + pieceBox.getZSpan() + ")");
		ctx.check(pieceBox.getYSpan() == 15, "y es alta: la torre llega a " + pieceBox.getYSpan() + " bloques");
		box = pieceBox;
		AABB area = new AABB(box.minX(), box.minY(), box.minZ(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1);

		// ---- bloques clave ----
		BlockPos chest = null, barrel = null, signal = null;
		java.util.Set<String> storageBlocks = new java.util.TreeSet<>();
		int soulFire = 0, ladders = 0;
		for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
			var state = level.getBlockState(pos);
			// Con Lootr instalado, el cofre y el barril de las estructuras pasan a ser cofres INDIVIDUALES por jugador.
			String blockPath = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
			if (blockPath.contains("chest") || blockPath.contains("barrel") || blockPath.contains("loot")) {
				storageBlocks.add(blockPath);
			}
			if (blockPath.equals("chest") || blockPath.equals("lootr_chest")) {   // Lootr usa el mismo nombre en su propio espacio
				chest = pos.immutable();
			} else if (blockPath.equals("barrel") || blockPath.equals("lootr_barrel")) {
				barrel = pos.immutable();
			} else if (state.is(Blocks.SOUL_CAMPFIRE)) {
				soulFire++;
				if (state.getValue(net.minecraft.world.level.block.CampfireBlock.SIGNAL_FIRE)) {
					signal = pos.immutable();
				}
			} else if (state.is(Blocks.LADDER)) {
				ladders++;
			}
		}
		ctx.check(chest != null, "tiene el cofre del altar");
		if (chest != null) {
			ctx.check(true, "(tipo de cofre: " + net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(chest).getBlock()) + ")");
		}
		ctx.check(barrel != null, "tiene el barril pequeno de la torre");
		ctx.check(signal != null, "la torre tiene una hoguera de almas de SENAL (el humo que se ve desde lejos)");
		ctx.check(soulFire >= 2, "y un brasero en el patio (hogueras de almas: " + soulFire + ")");
		ctx.check(ladders >= 8, "se puede subir a la torre (escalera de mano: " + ladders + " bloques)");
		if (signal != null) {
			ctx.check(level.getBlockState(signal.below()).is(Blocks.HAY_BLOCK), "la hoguera de senal descansa sobre un fardo de heno");
			ctx.check(signal.getY() - box.minY() >= 10, "y esta en lo alto de la torre (y+" + (signal.getY() - box.minY()) + ")");
		}

		// ---- el botin ----
		var lootKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, PurgatorioCore.id("chests/ruina_de_ceniza"));
		if (chest != null) {
			BlockEntity be = level.getBlockEntity(chest);
			if (be instanceof RandomizableContainerBlockEntity rc) {
				ctx.eq(lootKey, rc.getLootTable(), "el cofre usa la tabla purgatorio:chests/ruina_de_ceniza");
			} else {
				ctx.check(be != null, "el cofre tiene bloque-entidad (con Lootr se sustituye por su cofre individual: " + be + ")");
			}
		}
		LootTable table = ctx.server.reloadableRegistries().getLootTable(lootKey);
		ctx.check(table != LootTable.EMPTY, "la tabla de botin del cofre esta cargada");
		Ctx.Mock m = ctx.join("ruina-test-a");
		ServerPlayer p = m.player();
		boolean colmilloEverytime = true, bookEverytime = true, shardsOk = true;
		int books = 0;
		String shardDiag = "", bookDiag = "";
		for (int i = 0; i < 40; i++) {
			LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(chest != null ? chest : box.getCenter()))
				.withOptionalParameter(LootContextParams.THIS_ENTITY, p).create(LootContextParamSets.CHEST);
			List<ItemStack> rolled = table.getRandomItems(params);
			colmilloEverytime &= rolled.stream().anyMatch(s -> s.getItem() instanceof com.purgatorio.core.item.ColmilloDeCeniza);
			int shards = rolled.stream().filter(s -> ForgeSuite.shard() != null && s.is(ForgeSuite.shard())).mapToInt(ItemStack::getCount).sum();
			if (shards < 2 || shards > 4) {
				shardsOk = false;
				shardDiag = "tirada " + i + ": " + shards + " esquirlas en " + rolled;
			}
			for (ItemStack s : rolled) {
				if (s.is(net.minecraft.world.item.Items.WRITTEN_BOOK)) {
					books++;
					var content = s.get(DataComponents.WRITTEN_BOOK_CONTENT);
					boolean good = content != null && !content.title().raw().isEmpty() && !content.pages().isEmpty();
					if (!good) {
						bookDiag = "libro sin contenido valido: " + s + " content=" + content;
					}
					bookEverytime &= good;
				}
			}
		}
		ctx.check(colmilloEverytime, "el cofre SIEMPRE da el Colmillo de Ceniza (40 tiradas)");
		ctx.check(shardsOk, "y 2-4 Esquirlas de Brasa " + shardDiag);
		ctx.eq(40, books, "y siempre una nota escrita");
		ctx.check(bookEverytime, "con titulo y paginas legibles " + bookDiag);

		// ---- los habitantes ----
		List<Husk> guards = level.getEntitiesOfClass(Husk.class, area.inflate(2), h -> h.entityTags().contains(AcechadorBehavior.TAG));
		ctx.eq(2, guards.size(), "dos Acechadores vigilan la ruina");
		for (Husk g : guards) {
			ctx.near(36.0, g.getMaxHealth(), 1e-6, "cada uno con 36 de vida");
			ctx.check(g.getLootTable().isPresent(), "y su botin del datapack");
		}
		ctx.check(AcechadorBehavior.trackedCount() >= 2, "ambos estan en el seguimiento de comportamiento (" + AcechadorBehavior.trackedCount() + ")");

		// ---- pistas de exploracion: ceniza en el aire y campana lejana ----
		Vec3 far = new Vec3(box.getCenter().getX() + 40, box.minY() + 4, box.getCenter().getZ());
		p.snapTo(far.x, far.y, far.z, 0, 0);
		ctx.check(PlaceAmbience.nearestRuin(level, p) != null, "a 40 bloques el jugador 'siente' la ruina (consulta de chunks ya cargados)");
		int before = m.channel().outboundMessages().size();
		for (int i = 0; i < 60; i++) {
			PlaceAmbience.tickPlayer(ctx.server, p);
		}
		boolean ash = false, bell = false;
		for (Object o : m.channel().outboundMessages()) {
			ash |= o instanceof ClientboundLevelParticlesPacket;
			bell |= o instanceof ClientboundSoundPacket;
		}
		ctx.check(ash, "cerca de la ruina cae ceniza en el aire (particulas)");
		ctx.check(bell, "y se oye una campana lejana desde su direccion (sonido posicional)");
		p.snapTo(box.getCenter().getX() + 9000, box.minY() + 4, box.getCenter().getZ() + 9000, 0, 0);   // chunks sin generar
		int loadedBefore = level.getChunkSource().getLoadedChunksCount();
		ctx.check(PlaceAmbience.nearestRuin(level, p) == null, "lejos de la ruina no hay pistas");
		for (int i = 0; i < 20; i++) {
			PlaceAmbience.tickPlayer(ctx.server, p);
		}
		ctx.eq(loadedBefore, level.getChunkSource().getLoadedChunksCount(), "REGRESION: la consulta de ambiente NUNCA genera ni carga chunks");
		ctx.check(AlmaRules.MAX > 0, "(estructura verificada)");
	}
}
