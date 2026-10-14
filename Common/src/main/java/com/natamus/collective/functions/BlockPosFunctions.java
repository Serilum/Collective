package com.natamus.collective.functions;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.data.GlobalVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class BlockPosFunctions {
	// START: GET functions
	public static List<BlockPos> getBlocksAround(BlockPos pos, boolean down) {
		List<BlockPos> around = new ArrayList<BlockPos>();
		around.add(pos.north());
		around.add(pos.east());
		around.add(pos.south());
		around.add(pos.west());
		around.add(pos.above());
		if (down) {
			around.add(pos.below());
		}
		return around;
	}

	// START: RECURSIVE GET BLOCKS
	private static final HashMap<BlockPos, Integer> rgnbcount = new HashMap<BlockPos, Integer>();

	public static List<BlockPos> getBlocksNextToEachOther(Level level, BlockPos startPos, List<Block> possibleBlocks) {
		return getBlocksNextToEachOther(level, startPos, possibleBlocks, 50);
	}
	public static List<BlockPos> getBlocksNextToEachOther(Level level, BlockPos startPos, List<Block> possibleBlocks, int maxDistance) {
		List<BlockPos> checkedBlocks = new ArrayList<BlockPos>();
		List<BlockPos> blocksAround = new ArrayList<BlockPos>();
		if (possibleBlocks.contains(level.getBlockState(startPos).getBlock())) {
			blocksAround.add(startPos);
			checkedBlocks.add(startPos);
		}

		rgnbcount.put(startPos.immutable(), 0);
		recursiveGetNextBlocks(level, startPos, startPos, possibleBlocks, blocksAround, checkedBlocks, maxDistance);
		return blocksAround;
	}
	private static void recursiveGetNextBlocks(Level level, BlockPos startPos, BlockPos pos, List<Block> possibleBlocks, List<BlockPos> blocksAround, List<BlockPos> checkedBlocks, int maxDistance) {
		int rgnbc = rgnbcount.get(startPos);
		if (rgnbc > 100) {
			return;
		}
		rgnbcount.put(startPos, rgnbc + 1);

		List<BlockPos> possibleBlocksaround = getBlocksAround(pos, true);
		for (BlockPos pba : possibleBlocksaround) {
			if (checkedBlocks.contains(pba)) {
				continue;
			}
			checkedBlocks.add(pba);

			if (possibleBlocks.contains(level.getBlockState(pba).getBlock())) {
				if (!blocksAround.contains(pba)) {
					blocksAround.add(pba);
					if (BlockPosFunctions.withinDistance(startPos, pba, maxDistance)) {
						recursiveGetNextBlocks(level, startPos, pba, possibleBlocks, blocksAround, checkedBlocks, maxDistance);
					}
				}
			}
		}
	}


	private static final HashMap<BlockPos, Integer> rgnbtcount = new HashMap<BlockPos, Integer>();

	public static List<BlockPos> getBlocksNextToEachOtherTag(Level level, BlockPos startPos, List<TagKey<Block>> possibleBlockTags) {
		return getBlocksNextToEachOtherTag(level, startPos, possibleBlockTags, 50);
	}
	public static List<BlockPos> getBlocksNextToEachOtherTag(Level level, BlockPos startPos, List<TagKey<Block>> possibleBlockTags, int maxDistance) {
		List<BlockPos> checkedBlocks = new ArrayList<BlockPos>();
		List<BlockPos> blocksAround = new ArrayList<BlockPos>();

		BlockState startBlockState = level.getBlockState(startPos);
		for (TagKey<Block> blockTagKey : possibleBlockTags) {
			if (startBlockState.is(blockTagKey)) {
				blocksAround.add(startPos);
				checkedBlocks.add(startPos);
				break;
			}
		}

		rgnbtcount.put(startPos.immutable(), 0);
		recursiveGetNextBlocksTag(level, startPos, startPos, possibleBlockTags, blocksAround, checkedBlocks, maxDistance);
		return blocksAround;
	}
	private static void recursiveGetNextBlocksTag(Level level, BlockPos startPos, BlockPos pos, List<TagKey<Block>> possibleBlockTags, List<BlockPos> blocksAround, List<BlockPos> checkedBlocks, int maxDistance) {
		int rgnbc = rgnbtcount.get(startPos);
		if (rgnbc > 100) {
			return;
		}
		rgnbtcount.put(startPos, rgnbc + 1);

		List<BlockPos> possibleBlocksaround = getBlocksAround(pos, true);
		for (BlockPos pba : possibleBlocksaround) {
			if (checkedBlocks.contains(pba)) {
				continue;
			}
			checkedBlocks.add(pba);

			BlockState pbaState = level.getBlockState(pba);
			for (TagKey<Block> blockTagKey : possibleBlockTags) {
				if (pbaState.is(blockTagKey)) {
					if (!blocksAround.contains(pba)) {
						blocksAround.add(pba);
						if (BlockPosFunctions.withinDistance(startPos, pba, maxDistance)) {
							recursiveGetNextBlocksTag(level, startPos, pba, possibleBlockTags, blocksAround, checkedBlocks, maxDistance);
						}
					}
					break;
				}
			}
		}
	}


	private static final HashMap<BlockPos, Integer> rgnbmcount = new HashMap<BlockPos, Integer>();

	public static List<BlockPos> getBlocksNextToEachOtherMaterial(Level level, BlockPos startPos, List<MapColor> possibleMaterials) {
		return getBlocksNextToEachOtherMaterial(level, startPos, possibleMaterials, 50);
	}
	public static List<BlockPos> getBlocksNextToEachOtherMaterial(Level level, BlockPos startPos, List<MapColor> possibleMaterials, int maxDistance) {
		List<BlockPos> checkedBlocks = new ArrayList<BlockPos>();
		List<BlockPos> blocksAround = new ArrayList<BlockPos>();
		if (possibleMaterials.contains(level.getBlockState(startPos).getMapColor(level, startPos))) {
			blocksAround.add(startPos);
			checkedBlocks.add(startPos);
		}

		rgnbmcount.put(startPos.immutable(), 0);
		recursiveGetNextBlocksMaterial(level, startPos, startPos, possibleMaterials, blocksAround, checkedBlocks, maxDistance);
		return blocksAround;
	}
	private static void recursiveGetNextBlocksMaterial(Level level, BlockPos startPos, BlockPos pos, List<MapColor> possibleMaterials, List<BlockPos> blocksAround, List<BlockPos> checkedBlocks, int maxDistance) {
		int rgnbmc = rgnbmcount.get(startPos);
		if (rgnbmc > 100) {
			return;
		}
		rgnbmcount.put(startPos, rgnbmc + 1);

		List<BlockPos> possibleBlocksaround = getBlocksAround(pos, true);
		for (BlockPos pba : possibleBlocksaround) {
			if (checkedBlocks.contains(pba)) {
				continue;
			}
			checkedBlocks.add(pba);

			if (possibleMaterials.contains(level.getBlockState(pba).getMapColor(level, pba))) {
				if (!blocksAround.contains(pba)) {
					blocksAround.add(pba);
					if (BlockPosFunctions.withinDistance(startPos, pba, maxDistance)) {
						recursiveGetNextBlocksMaterial(level, startPos, pba, possibleMaterials, blocksAround, checkedBlocks, maxDistance);
					}
				}
			}
		}
	}
	// END RECURSIVE GET BLOCKS

	public static BlockPos getSurfaceBlockPos(ServerLevel serverLevel, int x, int z) {
		return getSurfaceBlockPos(serverLevel, x, z, false);
	}
	public static BlockPos getSurfaceBlockPos(ServerLevel serverLevel, int x, int z, boolean ignoreTrees) {
		int height = serverLevel.getHeight();
		int lowestY = serverLevel.getMinBuildHeight();

		BlockPos returnPos = new BlockPos(x, height-1, z);
		if (!WorldFunctions.isNether(serverLevel)) {
			BlockPos pos = new BlockPos(x, height, z);
			for (int y = height; y > lowestY; y--) {
				boolean continueCycle = false;

				BlockState blockState = serverLevel.getBlockState(pos);
				if (ignoreTrees) {
					Block block = blockState.getBlock();
					if (CompareBlockFunctions.isTreeLeaf(block) || CompareBlockFunctions.isTreeLog(block)) {
						continueCycle = true;
					}
				}

				if (!continueCycle) {
					MapColor material = blockState.getMapColor(serverLevel, pos);
					if (blockState.getLightBlock(serverLevel, pos) >= 15 || GlobalVariables.surfacematerials.contains(material)) {
						returnPos = pos.above().immutable();
						break;
					}
				}

				pos = pos.below();
			}
		}
		else {
			int maxHeight = 128;
			BlockPos pos = new BlockPos(x, lowestY, z);
			for (int y = lowestY; y < maxHeight; y++) {
				BlockState blockState = serverLevel.getBlockState(pos);
				if (blockState.getBlock().equals(Blocks.AIR)) {
					BlockState upstate = serverLevel.getBlockState(pos.above());
					if (upstate.getBlock().equals(Blocks.AIR)) {
						returnPos = pos.immutable();
						break;
					}
				}

				pos = pos.above();
			}
		}

		return returnPos;
	}

	public static BlockPos getCenterNearbyVillage(ServerLevel serverLevel) {
		return getNearbyVillage(serverLevel, new BlockPos(0, 0, 0));
	}
	public static BlockPos getNearbyVillage(ServerLevel serverLevel, BlockPos nearPos) {
		return getNearbyVillage(serverLevel, nearPos, "#minecraft:village");
	}
	public static BlockPos getNearbyVillage(ServerLevel serverLevel, BlockPos nearPos, String villageTag) {
		if (!serverLevel.getServer().getWorldData().worldGenOptions().generateStructures()) {
			return null;
		}

		HolderSet<Structure> villageStructures = getStructureHolderSet(serverLevel, villageTag);
		if (villageStructures == null) {
			return null;
		}

		return getSurfacePosOfNearestStructure(serverLevel, villageStructures, nearPos);
	}

	public static BlockPos getNearbyVillageInSameBiome(ServerLevel serverLevel, BlockPos nearPos) {
		return getNearbyVillageInSameBiome(serverLevel, nearPos, "#minecraft:village");
	}
	public static BlockPos getNearbyVillageInSameBiome(ServerLevel serverLevel, BlockPos nearPos, String villageTag) {
		if (!serverLevel.getServer().getWorldData().worldGenOptions().generateStructures()) {
			return null;
		}

		HolderSet<Structure> villageStructures = getStructureHolderSet(serverLevel, villageTag);
		if (villageStructures == null) {
			return null;
		}

		Holder<Biome> biome = serverLevel.getBiome(nearPos);
		List<Holder<Structure>> villagesInBiome = new ArrayList<>();
		for (Holder<Structure> village : villageStructures) {
			if (village.value().biomes().contains(biome)) {
				villagesInBiome.add(village);
			}
		}

		if (villagesInBiome.isEmpty()) {
			return null;
		}

		return getSurfacePosOfNearestStructure(serverLevel, HolderSet.direct(villagesInBiome), nearPos);
	}

	private static BlockPos getSurfacePosOfNearestStructure(ServerLevel serverLevel, HolderSet<Structure> structures, BlockPos nearPos) {
		Pair<BlockPos, Holder<Structure>> nearestStructure = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, structures, nearPos, 100, false);
		if (nearestStructure == null) {
			return null;
		}

		BlockPos structurePos = nearestStructure.getFirst();
		return getSurfaceBlockPos(serverLevel, structurePos.getX(), structurePos.getZ());
	}

	public static HolderSet<Structure> getStructureHolderSet(ServerLevel serverLevel, String structure) {
		String strippedStructure = structure.strip();
		ResourceLocation structureIdentifier = ResourceLocation.tryParse(strippedStructure.replace("#", ""));
		if (structureIdentifier == null) {
			return null;
		}

		Registry<Structure> structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
		if (strippedStructure.startsWith("#")) {
			Optional<HolderSet.Named<Structure>> optionalStructureTag = structureRegistry.getTag(TagKey.create(Registries.STRUCTURE, structureIdentifier));
			if (optionalStructureTag.isPresent()) {
				return optionalStructureTag.get();
			}
			return null;
		}

		Optional<Holder.Reference<Structure>> optionalStructure = structureRegistry.getHolder(ResourceKey.create(Registries.STRUCTURE, structureIdentifier));
		if (optionalStructure.isPresent()) {
			return HolderSet.direct(optionalStructure.get());
		}
		return null;
	}

	public static BlockPos getCenterNearbyBiome(ServerLevel serverLevel, String biome) {
		return getCenterNearbyBiome(serverLevel, biome, 6400, 16, false);
	}
	public static BlockPos getCenterNearbyBiome(ServerLevel serverLevel, String biome, int searchRadius, int minimumDistanceFromBiomeEdge, boolean preferLand) {
		return getNearbyBiome(serverLevel, new BlockPos(0, 0, 0), biome, searchRadius, minimumDistanceFromBiomeEdge, preferLand);
	}
	public static BlockPos getNearbyBiome(ServerLevel serverLevel, BlockPos nearPos, String biome) {
		return getNearbyBiome(serverLevel, nearPos, biome, 6400, 16, false);
	}
	public static BlockPos getNearbyBiome(ServerLevel serverLevel, BlockPos nearPos, String biome, int searchRadius, int minimumDistanceFromBiomeEdge, boolean preferLand) {
		Predicate<Holder<Biome>> biomePredicate = getBiomePredicate(biome);
		if (biomePredicate == null) {
			return null;
		}

		ServerChunkCache chunkSource = serverLevel.getChunkSource();
		ChunkGenerator chunkGenerator = chunkSource.getGenerator();
		BiomeSource biomeSource = chunkGenerator.getBiomeSource();
		if (!canBiomeSourceGenerate(biomeSource, biomePredicate)) {
			return null;
		}

		RandomState randomState = chunkSource.randomState();
		Climate.Sampler climateSampler = randomState.sampler();

		BlockPos firstColumnWithBiome = null;
		BlockPos firstLandColumnNearEdge = null;
		BlockPos firstWaterColumnAwayFromEdge = null;
		BlockPos firstWaterColumnNearEdge = null;
		int surfaceChecks = 0;

		for (BlockPos.MutableBlockPos sampleColumn : BlockPos.spiralAround(BlockPos.ZERO, searchRadius / 32, Direction.EAST, Direction.SOUTH)) {
			int x = nearPos.getX() + sampleColumn.getX() * 32;
			int z = nearPos.getZ() + sampleColumn.getZ() * 32;
			if (!isBiomeInColumn(serverLevel, biomeSource, climateSampler, biomePredicate, x, z)) {
				continue;
			}

			if (firstColumnWithBiome == null) {
				firstColumnWithBiome = new BlockPos(x, 0, z);
			}

			int surfaceY = chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, serverLevel, randomState);
			if (isBiomeAt(biomeSource, climateSampler, biomePredicate, x, surfaceY, z)) {
				boolean isAwayFromEdge = isBiomeAroundOnSurface(biomeSource, climateSampler, biomePredicate, x, surfaceY, z, minimumDistanceFromBiomeEdge);
				boolean isOnLand = !preferLand || isLandColumn(serverLevel, chunkGenerator, randomState, x, surfaceY, z);
				if (isAwayFromEdge && isOnLand) {
					return getSurfaceBlockPos(serverLevel, x, z);
				}

				BlockPos column = new BlockPos(x, 0, z);
				if (isOnLand && firstLandColumnNearEdge == null) {
					firstLandColumnNearEdge = column;
				}
				else if (!isOnLand && isAwayFromEdge && firstWaterColumnAwayFromEdge == null) {
					firstWaterColumnAwayFromEdge = column;
				}
				else if (!isOnLand && !isAwayFromEdge && firstWaterColumnNearEdge == null) {
					firstWaterColumnNearEdge = column;
				}
			}

			surfaceChecks += 1;
			if (surfaceChecks >= 512) {
				break;
			}
		}

		BlockPos bestColumn = getFirstFoundColumn(firstLandColumnNearEdge, firstWaterColumnAwayFromEdge, firstWaterColumnNearEdge, firstColumnWithBiome);
		if (bestColumn == null) {
			return null;
		}

		return getSurfaceBlockPos(serverLevel, bestColumn.getX(), bestColumn.getZ());
	}

	private static boolean isLandColumn(ServerLevel serverLevel, ChunkGenerator chunkGenerator, RandomState randomState, int x, int surfaceY, int z) {
		return chunkGenerator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, serverLevel, randomState) >= surfaceY;
	}

	private static BlockPos getFirstFoundColumn(BlockPos... columnsInOrderOfPreference) {
		for (BlockPos column : columnsInOrderOfPreference) {
			if (column != null) {
				return column;
			}
		}
		return null;
	}

	private static Predicate<Holder<Biome>> getBiomePredicate(String biome) {
		String strippedBiome = biome.strip();
		ResourceLocation biomeIdentifier = ResourceLocation.tryParse(strippedBiome.replace("#", ""));
		if (biomeIdentifier == null) {
			return null;
		}

		if (strippedBiome.startsWith("#")) {
			TagKey<Biome> biomeTag = TagKey.create(Registries.BIOME, biomeIdentifier);
			return biomeHolder -> biomeHolder.is(biomeTag);
		}

		ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, biomeIdentifier);
		return biomeHolder -> biomeHolder.is(biomeKey);
	}

	private static boolean canBiomeSourceGenerate(BiomeSource biomeSource, Predicate<Holder<Biome>> biomePredicate) {
		for (Holder<Biome> possibleBiome : biomeSource.possibleBiomes()) {
			if (biomePredicate.test(possibleBiome)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isBiomeInColumn(ServerLevel serverLevel, BiomeSource biomeSource, Climate.Sampler climateSampler, Predicate<Holder<Biome>> biomePredicate, int x, int z) {
		for (int y = serverLevel.getMinBuildHeight(); y < serverLevel.getMaxBuildHeight(); y += 64) {
			if (isBiomeAt(biomeSource, climateSampler, biomePredicate, x, y, z)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isBiomeAroundOnSurface(BiomeSource biomeSource, Climate.Sampler climateSampler, Predicate<Holder<Biome>> biomePredicate, int x, int surfaceY, int z, int distance) {
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			int aroundX = x + direction.getStepX() * distance;
			int aroundZ = z + direction.getStepZ() * distance;
			if (!isBiomeAt(biomeSource, climateSampler, biomePredicate, aroundX, surfaceY, aroundZ)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isBiomeAt(BiomeSource biomeSource, Climate.Sampler climateSampler, Predicate<Holder<Biome>> biomePredicate, int x, int y, int z) {
		return biomePredicate.test(biomeSource.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), climateSampler));
	}

	public static BlockPos getCenterNearbyStructure(ServerLevel serverLevel, HolderSet<Structure> structure) {
		return getNearbyStructure(serverLevel, structure, new BlockPos(0, 0, 0));
	}
	public static BlockPos getNearbyStructure(ServerLevel serverLevel, HolderSet<Structure> structure, BlockPos nearPos) {
		return getNearbyStructure(serverLevel, structure, nearPos, 9999);
	}
	public static BlockPos getNearbyStructure(ServerLevel serverLevel, HolderSet<Structure> structure, BlockPos nearPos, int radius) {
		Pair<BlockPos, Holder<Structure>> pair = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, structure, nearPos, radius, false);
		if (pair == null) {
			return null;
		}

		BlockPos villagePos = pair.getFirst();
		if (villagePos == null) {
			return null;
		}

		BlockPos spawnpos = null;
		for (int y = serverLevel.getHeight()-1; y > 0; y--) {
			BlockPos checkpos = new BlockPos(villagePos.getX(), y, villagePos.getZ());
			if (serverLevel.getBlockState(checkpos).getBlock().equals(Blocks.AIR)) {
				continue;
			}
			spawnpos = checkpos.above().immutable();
			break;
		}

		return spawnpos;
	}

	public static BlockPos getBlockPlayerIsLookingAt(Level level, Player player, boolean stopOnLiquid) {
		HitResult raytraceresult = RayTraceFunctions.rayTrace(level, player, stopOnLiquid);
		double posX = raytraceresult.getLocation().x;
		double posY = Math.floor(raytraceresult.getLocation().y);
		double posZ = raytraceresult.getLocation().z;

		return BlockPos.containing(posX, posY, posZ);
	}

	public static BlockPos getRandomCoordinatesInNearestUngeneratedChunk(ServerLevel serverLevel, BlockPos aroundPosition) {
		List<String> regionList = new ArrayList<String>();

		try {
			File regionFolder = new File(WorldFunctions.getWorldPath(serverLevel) + File.separator + "region");
			File[] listOfRegionFiles = regionFolder.listFiles();

			for (File regionFile : listOfRegionFiles) {
				if (regionFile.isFile()) {
					regionList.add(regionFile.getName().replaceAll("r.", "").replaceAll(".mca", ""));
				}
			}
		}
		catch(NullPointerException ignored) {
			return null;
		}

		ChunkPos chunkPos = serverLevel.getChunkAt(aroundPosition).getPos();
		int curRegionX = chunkPos.getRegionX();
		int curRegionZ = chunkPos.getRegionZ();

		int currentRange = 1;
		int loops = 0;

		String closestUngeneratedRegionString = "";
		while (closestUngeneratedRegionString.equals("")) {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					int regionX = curRegionX + (x*currentRange);
					int regionZ = curRegionZ + (z*currentRange);

					String regionString = regionX + "." + regionZ;
					if (!regionList.contains(regionString)) {
						closestUngeneratedRegionString = regionString;
						break;
					}
				}
				if (!closestUngeneratedRegionString.equals("")) {
					break;
				}
			}

			currentRange += 1;
			loops += 1;

			if (loops > 50) {
				return null;
			}
		}

		int outputRegionX, outputRegionZ;

		String[] cursspl = closestUngeneratedRegionString.split("\\.");
		try {
			outputRegionX = Integer.parseInt(cursspl[0]);
			outputRegionZ = Integer.parseInt(cursspl[1]);
		}
		catch (NumberFormatException ex) {
			return null;
		}

		int minXRange = (outputRegionX * 512) - 256;
		int maxXRange = (outputRegionX * 512) + 256;
		int minZRange = (outputRegionZ * 512) - 256;
		int maxZRange = (outputRegionZ * 512) + 256;

		int randomXCoord = ThreadLocalRandom.current().nextInt(minXRange, maxXRange + 1);
		int randomZCoord = ThreadLocalRandom.current().nextInt(minZRange, maxZRange + 1);
		int randomYCoord = BlockPosFunctions.getSurfaceBlockPos(serverLevel, randomXCoord, randomZCoord).getY();

		return new BlockPos(randomXCoord, randomYCoord, randomZCoord);
	}
	// END: GET functions


	// START: CHECK functions
	public static Boolean isOnSurface(Level level, BlockPos pos) {
		return level.canSeeSky(pos);
	}
	public static Boolean isOnSurface(Level level, Vec3 vecPos) {
		return isOnSurface(level, BlockPos.containing(vecPos.x, vecPos.y, vecPos.z));
	}


	public static Boolean withinDistance(BlockPos start, BlockPos end, int distance) {
		return withinDistance(start, end, (double) distance);
	}
	public static Boolean withinDistance(BlockPos start, BlockPos end, double distance) {
		return start.closerThan(end, distance);
	}
	// END: CHECK functions

	public static BlockPos getBlockPosFromHitResult(HitResult hitResult) {
		Vec3 vec = hitResult.getLocation();
		return BlockPos.containing(vec.x, vec.y, vec.z);
	}
}