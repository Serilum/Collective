package com.natamus.collective.functions;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.data.GlobalVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
		BlockPos closestvillage = null;
		if (!serverLevel.getServer().getWorldData().worldGenOptions().generateStructures()) {
			return null;
		}

		String rawOutput = CommandFunctions.getRawCommandOutput(serverLevel, Vec3.atBottomCenterOf(nearPos), "/locate structure #minecraft:village");

		if (rawOutput.contains("[") && rawOutput.contains("]") && rawOutput.contains(", ")) {
			String[] coords;
			try {
				if (rawOutput.contains(":")) {
					rawOutput = rawOutput.split(":", 2)[1];
				}

				String rawcoords = rawOutput.split("\\[")[1].split("]")[0];
				coords = rawcoords.split(", ");
			}
			catch (IndexOutOfBoundsException ex) {
				return null;
			}

			if (coords.length == 3) {
				String sx = coords[0];
				String sz = coords[2];
				if (NumberFunctions.isNumeric(sx) && NumberFunctions.isNumeric(sz)) {
					return getSurfaceBlockPos(serverLevel, Integer.parseInt(sx), Integer.parseInt(sz));
				}
			}
		}

		return closestvillage;
	}

	public static BlockPos getCenterNearbyBiome(ServerLevel serverLevel, String biome) {
		return getNearbyBiome(serverLevel, new BlockPos(0, 0, 0), biome);
	}
	public static BlockPos getNearbyBiome(ServerLevel serverLevel, BlockPos nearPos, String biome) {
		String rawOutput = CommandFunctions.getRawCommandOutput(serverLevel, Vec3.atBottomCenterOf(nearPos), "/locate biome " + biome);

		if (rawOutput.contains("nearest") && rawOutput.contains("[")) {
			String rawcoords = rawOutput.split("nearest")[1].split("\\[")[1].split("]")[0];
			String[] coords = rawcoords.split(", ");
			if (coords.length == 3) {
				String sx = coords[0];
				String sz = coords[2];
				if (NumberFunctions.isNumeric(sx) && NumberFunctions.isNumeric(sz)) {
					return getSurfaceBlockPos(serverLevel, Integer.parseInt(sx), Integer.parseInt(sz));
				}
			}
		}

		return null;
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