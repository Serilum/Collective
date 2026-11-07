package com.natamus.collective.functions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class VillageSpawnFunctions {
	private static final String villageSpawnPointModId = "villagespawnpoint";
	private static final String defaultVillages = "#minecraft:village";

	public static BlockPos getVillageSpawnPos(ServerLevel serverLevel, BlockPos nearPos) {
		return getVillageSpawnPos(serverLevel, nearPos, village -> true);
	}

	public static BlockPos getVillageSpawnPosInSameBiome(ServerLevel serverLevel, BlockPos nearPos) {
		Holder<Biome> biome = serverLevel.getBiome(nearPos);
		return getVillageSpawnPos(serverLevel, nearPos, village -> village.value().biomes().contains(biome));
	}

	private static BlockPos getVillageSpawnPos(ServerLevel serverLevel, BlockPos nearPos, Predicate<Holder<Structure>> villageFilter) {
		if (!serverLevel.getServer().getWorldGenSettings().options().generateStructures()) {
			return null;
		}

		String villageStructures = getVillageStructures();
		String blacklistedVillageStructures = getBlacklistedVillageStructures();
		HolderSet<Structure> villages = getAllowedVillages(serverLevel, villageStructures, blacklistedVillageStructures, villageFilter);
		BlockPos spawnPos = findVillageSpawnPos(serverLevel, villages, nearPos);
		if (spawnPos != null) {
			return spawnPos;
		}

		if (!useDefaultVillagesAsBackup() || villageStructures.equals(defaultVillages)) {
			return null;
		}

		HolderSet<Structure> backupVillages = getAllowedVillages(serverLevel, defaultVillages, blacklistedVillageStructures, villageFilter);
		return findVillageSpawnPos(serverLevel, backupVillages, nearPos);
	}

	private static BlockPos findVillageSpawnPos(ServerLevel serverLevel, HolderSet<Structure> villages, BlockPos nearPos) {
		if (villages.size() == 0) {
			return null;
		}

		long searchDeadline = System.currentTimeMillis() + getMaximumSearchSeconds() * 1000L;
		StructureStart villageStart = BlockPosFunctions.getNearestStructureStart(serverLevel, villages, nearPos, 100, searchDeadline);
		if (villageStart == null) {
			return null;
		}

		BlockPos villageCenter = BlockPosFunctions.getStructureStartCenter(villageStart);
		BlockPos surfacePos = BlockPosFunctions.getSurfaceBlockPos(serverLevel, villageCenter.getX(), villageCenter.getZ());
		if (!spawnInsideUndergroundVillages()) {
			return surfacePos;
		}

		BlockPos posInsideVillage = getHighestStandablePosInside(serverLevel, villageStart.getPieces().get(0).getBoundingBox());
		if (posInsideVillage == null || !isUnderground(serverLevel, posInsideVillage)) {
			return surfacePos;
		}

		return posInsideVillage;
	}

	private static String getVillageStructures() {
		return ConfigFunctions.getConfigString(villageSpawnPointModId, "locateVillageTag", defaultVillages);
	}

	private static String getBlacklistedVillageStructures() {
		return ConfigFunctions.getConfigString(villageSpawnPointModId, "blacklistedVillageStructures", "repurposed_structures:village_ocean,repurposed_structures:village_mushroom,integrated_villages:airship_village");
	}

	private static boolean useDefaultVillagesAsBackup() {
		return ConfigFunctions.getConfigBoolean(villageSpawnPointModId, "keepDefaultVillageTagBackup", true);
	}

	private static int getMaximumSearchSeconds() {
		return ConfigFunctions.getConfigInt(villageSpawnPointModId, "maximumVillageSearchSeconds", 60);
	}

	private static boolean spawnInsideUndergroundVillages() {
		return ConfigFunctions.getConfigBoolean(villageSpawnPointModId, "spawnNewPlayersInsideUndergroundVillages", true);
	}

	public static HolderSet<Structure> getAllowedVillages(ServerLevel serverLevel, String villageEntries, String blacklistedEntries) {
		return getAllowedVillages(serverLevel, villageEntries, blacklistedEntries, village -> true);
	}
	private static HolderSet<Structure> getAllowedVillages(ServerLevel serverLevel, String villageEntries, String blacklistedEntries, Predicate<Holder<Structure>> villageFilter) {
		List<HolderSet<Structure>> blacklistedStructures = getStructureHolderSets(serverLevel, blacklistedEntries);
		List<Holder<Structure>> allowedVillages = new ArrayList<>();

		for (HolderSet<Structure> structures : getStructureHolderSets(serverLevel, villageEntries)) {
			for (Holder<Structure> village : structures) {
				if (villageFilter.test(village) && !isBlacklisted(village, blacklistedStructures) && !allowedVillages.contains(village)) {
					allowedVillages.add(village);
				}
			}
		}

		return HolderSet.direct(allowedVillages);
	}

	private static List<HolderSet<Structure>> getStructureHolderSets(ServerLevel serverLevel, String commaSeparatedEntries) {
		List<HolderSet<Structure>> structureHolderSets = new ArrayList<>();
		for (String rawEntry : commaSeparatedEntries.split(",")) {
			if (rawEntry.isBlank()) {
				continue;
			}

			HolderSet<Structure> structures = BlockPosFunctions.getStructureHolderSet(serverLevel, rawEntry.strip());
			if (structures != null) {
				structureHolderSets.add(structures);
			}
		}

		return structureHolderSets;
	}

	private static boolean isBlacklisted(Holder<Structure> village, List<HolderSet<Structure>> blacklistedStructures) {
		for (HolderSet<Structure> structures : blacklistedStructures) {
			if (structures.contains(village)) {
				return true;
			}
		}
		return false;
	}

	public static BlockPos getHighestStandablePosInside(ServerLevel serverLevel, BoundingBox boundingBox) {
		int columnRadius = Math.min(16, Math.max(boundingBox.getXSpan(), boundingBox.getZSpan()) / 2);
		for (BlockPos.MutableBlockPos column : BlockPos.spiralAround(boundingBox.getCenter(), columnRadius, Direction.EAST, Direction.SOUTH)) {
			if (!boundingBox.isInside(column.getX(), boundingBox.minY(), column.getZ())) {
				continue;
			}

			for (int y = boundingBox.maxY(); y > boundingBox.minY(); y--) {
				BlockPos feetPos = new BlockPos(column.getX(), y, column.getZ());
				if (isStandable(serverLevel, feetPos)) {
					return feetPos;
				}
			}
		}

		return null;
	}

	private static boolean isStandable(ServerLevel serverLevel, BlockPos feetPos) {
		BlockPos groundPos = feetPos.below();
		BlockState groundState = serverLevel.getBlockState(groundPos);
		if (!Block.isFaceFull(groundState.getCollisionShape(serverLevel, groundPos), Direction.UP)) {
			return false;
		}

		return isOpenSpace(serverLevel, feetPos) && isOpenSpace(serverLevel, feetPos.above());
	}

	private static boolean isOpenSpace(ServerLevel serverLevel, BlockPos pos) {
		BlockState blockState = serverLevel.getBlockState(pos);
		return blockState.getCollisionShape(serverLevel, pos).isEmpty() && blockState.getFluidState().isEmpty();
	}

	public static boolean isUnderground(ServerLevel serverLevel, BlockPos feetPos) {
		return serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, feetPos.getX(), feetPos.getZ()) > feetPos.getY() + 2;
	}
}
