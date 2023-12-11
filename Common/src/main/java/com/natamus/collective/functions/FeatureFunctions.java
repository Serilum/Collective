package com.natamus.collective.functions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.natamus.collective.data.GlobalVariables;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class FeatureFunctions {
	public static boolean placeBonusChest(Level world, BlockPos blockposIn) {
		ChunkPos chunkpos = new ChunkPos(blockposIn);
		List<Integer> list = IntStream.rangeClosed(chunkpos.getMinBlockX(), chunkpos.getMaxBlockX()).boxed().collect(Collectors.toList());
		Collections.shuffle(list, GlobalVariables.random);
		List<Integer> list1 = IntStream.rangeClosed(chunkpos.getMinBlockZ(), chunkpos.getMaxBlockZ()).boxed().collect(Collectors.toList());
		Collections.shuffle(list1, GlobalVariables.random);
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
		
		for(Integer integer : list) {
			for(Integer integer1 : list1) {
				blockpos$mutable.set(integer, 0, integer1);
				BlockPos blockpos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos$mutable);
				if (world.isEmptyBlock(blockpos) || world.getBlockState(blockpos).getCollisionShape(world, blockpos).isEmpty()) {
					world.setBlock(blockpos, Blocks.CHEST.defaultBlockState(), 2);
					RandomizableContainerBlockEntity.setLootTable(world, GlobalVariables.random, blockpos, BuiltInLootTables.SPAWN_BONUS_CHEST);
					BlockState blockstate = Blocks.TORCH.defaultBlockState();
					
					for(Direction direction : Direction.Plane.HORIZONTAL) {
						BlockPos blockpos1 = blockpos.relative(direction);
						if (blockstate.canSurvive(world, blockpos1)) {
							world.setBlock(blockpos1, blockstate, 2);
						}
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
}
