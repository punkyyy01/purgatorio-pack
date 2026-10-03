package com.purgatorio.coretest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

/** Localiza donde genera el mundo de pruebas La Ruina de Ceniza (determinista por la semilla). */
final class RuinHelper {
	static final Identifier ID = Identifier.fromNamespaceAndPath("purgatorio", "ruina_de_ceniza");

	private RuinHelper() {
	}

	static Structure structure(ServerLevel level) {
		return level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(ID);
	}

	/** Chunk donde la colocacion aleatoria de la celda (0,0) situa el inicio de la ruina. */
	static ChunkPos chunk(ServerLevel level) {
		Holder<Structure> holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(
			net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE, ID));
		var state = level.getChunkSource().getGeneratorState();
		var placement = (RandomSpreadStructurePlacement) state.getPlacementsForStructure(holder).get(0);
		return placement.getPotentialStructureChunk(state.getLevelSeed(), 0, 0);
	}

	static StructureStart start(ServerLevel level) {
		ChunkPos c = chunk(level);
		for (StructureStart s : level.structureManager().startsForStructure(c.x(), c.z(), structure(level))) {
			if (s.isValid()) {
				return s;
			}
		}
		return null;
	}

	/** Caja de la pieza real (17x15x17). La caja del "inicio" es mayor (incluye margen) y no sirve para ubicar al jugador dentro. */
	static net.minecraft.world.level.levelgen.structure.BoundingBox pieceBox(ServerLevel level) {
		StructureStart s = start(level);
		return s == null ? null : s.getPieces().get(0).getBoundingBox();
	}

	static BlockPos center(ServerLevel level) {
		var box = pieceBox(level);
		return box == null ? null : box.getCenter();
	}

	/** Un punto del suelo interior de la ruina (donde se camina). */
	static net.minecraft.world.phys.Vec3 inside(ServerLevel level) {
		var box = pieceBox(level);
		return new net.minecraft.world.phys.Vec3(box.getCenter().getX() + 0.5, box.minY() + 3, box.getCenter().getZ() + 0.5);
	}
}
