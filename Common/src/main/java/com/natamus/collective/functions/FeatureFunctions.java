package com.natamus.collective.functions;

import com.natamus.collective.data.GlobalVariables;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.stream.IntStream;

public class FeatureFunctions {
	public static boolean placeBonusChest(Level level, BlockPos blockposIn) {
        ChunkPos chunkPos = new ChunkPos(blockposIn);
        IntArrayList list0 = Util.toShuffledList(IntStream.rangeClosed(chunkPos.getMinBlockX(), chunkPos.getMaxBlockX()), GlobalVariables.randomSource);
        IntArrayList list1 = Util.toShuffledList(IntStream.rangeClosed(chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ()), GlobalVariables.randomSource);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Integer i : list0) {

			for (Integer j : list1) {
				mutableBlockPos.set(i, 0, j);
				BlockPos heightMapPos = level.getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos);
				if (level.isEmptyBlock(heightMapPos) || level.getBlockState(heightMapPos).getCollisionShape(level, heightMapPos).isEmpty()) {
					level.setBlock(heightMapPos, Blocks.CHEST.defaultBlockState(), 2);
					RandomizableContainer.setBlockEntityLootTable(level, GlobalVariables.randomSource, heightMapPos, BuiltInLootTables.SPAWN_BONUS_CHEST);
					BlockState blockState = Blocks.TORCH.defaultBlockState();

					for (Direction direction : Direction.Plane.HORIZONTAL) {
						BlockPos blockPos = heightMapPos.relative(direction);
						if (blockState.canSurvive(level, blockPos)) {
							level.setBlock(blockPos, blockState, 2);
						}
					}

					return true;
				}
			}
		}

        return false;
	}
}
