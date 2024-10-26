package com.natamus.collective.functions;

import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.data.GlobalVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class FABFunctions {
	private static final Map<Block, WeakHashMap<Level, List<BlockPos>>> getMapFromBlock = new HashMap<Block, WeakHashMap<Level, List<BlockPos>>>();
	private static final WeakHashMap<Level, Map<Date, BlockPos>> timeoutpositions = new WeakHashMap<Level, Map<Date, BlockPos>>();

	public static List<BlockEntity> getBlockEntitiesAroundPosition(Level level, BlockPos pos, Integer radius) {
		List<BlockEntity> blockentities = new ArrayList<BlockEntity>();

		int chunkradius = (int)Math.ceil(radius/16.0);
		int chunkPosX = pos.getX() >> 4;
		int chunkPosZ = pos.getZ() >> 4;

		for (int x = chunkPosX - chunkradius; x < chunkPosX + chunkradius; x++) {
			for (int z = chunkPosZ - chunkradius; z < chunkPosZ + chunkradius; z++) {
				LevelChunk levelChunk = level.getChunk(x, z);
				if (!level.isLoaded(levelChunk.getPos().getWorldPosition())) {
					continue;
				}

				for (BlockEntity be : levelChunk.getBlockEntities().values()) {
					if (!blockentities.contains(be)) {
						blockentities.add(be);
					}
				}
			}
		}

		return blockentities;
	}

	public static List<BlockPos> getAllTaggedTileEntityPositionsNearbyEntity(TagKey<Block> tetag, Integer radius, Level level, Entity entity) {
		return getAllTaggedTileEntityPositionsNearbyPosition(tetag, radius, level, entity.blockPosition());
	}

	public static List<BlockPos> getAllTaggedTileEntityPositionsNearbyPosition(TagKey<Block> tetag, Integer radius, Level level, BlockPos pos) {
		List<BlockPos> nearbypositions = new ArrayList<BlockPos>();
		List<BlockEntity> blockentities = getBlockEntitiesAroundPosition(level, pos, radius);

		for (BlockEntity loadedtileentity : blockentities) {
			BlockState loadedtilestate = loadedtileentity.getBlockState();
			if (loadedtilestate == null) {
				continue;
			}

			if (loadedtilestate.is(tetag)) {
				BlockPos ltepos = loadedtileentity.getBlockPos();
				if (ltepos.closerThan(new Vec3i(pos.getX(), pos.getY(), pos.getZ()), radius)) {
					nearbypositions.add(loadedtileentity.getBlockPos());
				}
			}
		}

		return nearbypositions;
	}

	public static List<BlockPos> getAllTileEntityPositionsNearbyEntity(BlockEntityType<?> tetype, Integer radius, Level level, Entity entity) {
		return getAllTileEntityPositionsNearbyPosition(tetype, radius, level, entity.blockPosition());
	}

	public static List<BlockPos> getAllTileEntityPositionsNearbyPosition(BlockEntityType<?> tetype, Integer radius, Level level, BlockPos pos) {
		List<BlockPos> nearbypositions = new ArrayList<BlockPos>();
		List<BlockEntity> blockentities = getBlockEntitiesAroundPosition(level, pos, radius);

		for (BlockEntity loadedtileentity : blockentities) {
			BlockEntityType<?> loadedtiletype = loadedtileentity.getType();
			if (loadedtiletype == null) {
				continue;
			}

			if (loadedtiletype.equals(tetype)) {
				BlockPos ltepos = loadedtileentity.getBlockPos();
				if (ltepos.closerThan(new Vec3i(pos.getX(), pos.getY(), pos.getZ()), radius)) {
					nearbypositions.add(loadedtileentity.getBlockPos());
				}
			}
		}

		return nearbypositions;
	}

	public static BlockPos getRequestedBlockAroundEntitySpawn(Block rawqueryblock, Integer radius, Double radiusmodifier, Level level, Entity entity) {
		Block requestedblock = processCommonBlock(rawqueryblock);
		WeakHashMap<Level, List<BlockPos>> levelblocks = getMap(requestedblock);

		BlockPos epos = entity.blockPosition();

		List<BlockPos> currentblocks;
		BlockPos removeblockpos = null;

		if (levelblocks.containsKey(level)) {
			currentblocks = levelblocks.get(level);

			List<BlockPos> cbtoremove = new ArrayList<BlockPos>();
			for (BlockPos cblock : currentblocks) {
				if (!level.getChunkSource().hasChunk(cblock.getX() >> 4, cblock.getZ() >> 4)) {
					cbtoremove.add(cblock);
					continue;
				}
				if (!level.getBlockState(cblock).getBlock().equals(requestedblock)) {
					cbtoremove.add(cblock);
					continue;
				}

				if (cblock.closerThan(epos, radius*radiusmodifier)) {
					return cblock.immutable();
				}
			}

			if (cbtoremove.size() > 0) {
				for (BlockPos tr : cbtoremove) {
					currentblocks.remove(tr);
				}
			}
		}
		else {
			currentblocks = new ArrayList<BlockPos>();
		}

		// Timeout function which prevents too many of the loop through blocks.
		Map<Date, BlockPos> timeouts;
		if (timeoutpositions.containsKey(level)) {
			timeouts = timeoutpositions.get(level);

			List<Date> totoremove = new ArrayList<Date>();
			if (timeouts.size() > 0) {
				Date now = new Date();
				for (Date todate : timeouts.keySet()) {
					BlockPos toepos = timeouts.get(todate);
					if (removeblockpos != null) {
						if (toepos.closerThan(removeblockpos, 64)) {
							totoremove.add(todate);
						}
					}
					long ms = (now.getTime()-todate.getTime());
					if (ms > CollectiveConfigHandler.findABlockCheckAroundEntitiesDelayMs) {
						totoremove.add(todate);
						continue;
					}
					if (toepos.closerThan(epos, radius*radiusmodifier)) {
						return null;
					}
				}
			}

			if (totoremove.size() > 0) {
				for (Date tr : totoremove) {
					timeouts.remove(tr);
				}
			}
		}
		else {
			timeouts = new HashMap<Date, BlockPos>();
		}

		if (GlobalVariables.blocksWithTileEntity.containsKey(requestedblock)) {
			List<BlockEntity> blockentities = getBlockEntitiesAroundPosition(level, epos, radius);
			BlockEntityType<?> tiletypetofind = GlobalVariables.blocksWithTileEntity.get(requestedblock);

			for (BlockEntity loadedtileentity : blockentities) {
				BlockEntityType<?> loadedtiletype = loadedtileentity.getType();
				if (loadedtiletype == null) {
					continue;
				}

				if (loadedtiletype.equals(tiletypetofind)) {
					BlockPos ltepos = loadedtileentity.getBlockPos();

					if (ltepos.closerThan(epos, radius*radiusmodifier)) {
						currentblocks.add(ltepos.immutable());
						levelblocks.put(level, currentblocks);
						getMapFromBlock.put(requestedblock, levelblocks);

						return ltepos.immutable();
					}
				}
			}
		}
		else {
			int r = radius;
			for (int x = -r; x < r; x++) {
				for (int y = -r; y < r; y++) {
					for (int z = -r; z < r; z++) {
						BlockPos cpos = epos.east(x).north(y).above(z);
						BlockState state = level.getBlockState(cpos);
						if (state.getBlock().equals(requestedblock)) {
							currentblocks.add(cpos.immutable());
							levelblocks.put(level, currentblocks);
							getMapFromBlock.put(requestedblock, levelblocks);

							return cpos.immutable();
						}
					}
				}
			}
		}

		timeouts.put(new Date(), epos.immutable());
		timeoutpositions.put(level, timeouts);
		return null;
	}

	public static BlockPos updatePlacedBlock(Block requestedblock, BlockPos bpos, Level level) {
		BlockState state = level.getBlockState(bpos);
		if (state.getBlock().equals(requestedblock)) {
			WeakHashMap<Level, List<BlockPos>> levelblocks = getMap(requestedblock);

			List<BlockPos> currentblocks;
			if (levelblocks.containsKey(level)) {
				currentblocks = levelblocks.get(level);
			}
			else {
				currentblocks = new ArrayList<BlockPos>();
			}

			if (!currentblocks.contains(bpos)) {
				currentblocks.add(bpos);
				levelblocks.put(level, currentblocks);
				getMapFromBlock.put(requestedblock, levelblocks);
			}
			return bpos;
		}

		return null;
	}

	// Internal util functions
	private static WeakHashMap<Level,List<BlockPos>> getMap(Block requestedblock) {
		WeakHashMap<Level, List<BlockPos>> levelblocks;
		if (getMapFromBlock.containsKey(requestedblock)) {
			levelblocks = getMapFromBlock.get(requestedblock);
		}
		else {
			levelblocks = new WeakHashMap<Level, List<BlockPos>>();
		}
		return levelblocks;
	}

	private static Block processCommonBlock(Block requestedblock) {
		Block blocktoreturn = requestedblock;
		if (requestedblock instanceof StandingSignBlock || requestedblock instanceof WallSignBlock) {
			blocktoreturn = Blocks.OAK_SIGN;
		}

		return blocktoreturn;
	}
}
