package com.natamus.collective.functions;

import com.natamus.collective.data.GlobalVariables;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityFunctions {
	public static void updateTileEntity(Level world, BlockPos pos) {
		BlockEntity tileentity = world.getBlockEntity(pos); // CHECK FOR NULL
		updateTileEntity(world, pos, tileentity);
	}
	public static void updateTileEntity(Level world, BlockPos pos, BlockEntity tileentity) {
		BlockState state = world.getBlockState(pos);
		updateTileEntity(world, pos, state, tileentity);
	}
	public static void updateTileEntity(Level world, BlockPos pos, BlockState state, BlockEntity tileentity) {
		world.setBlocksDirty(pos, state, state);
		world.sendBlockUpdated(pos, state, state, 3);
		tileentity.setChanged();
	}

	public static void setMobSpawnerDelay(BaseSpawner spawner, int delay) {
		spawner.spawnDelay = delay;
	}
	public static void resetMobSpawnerDelay(BaseSpawner spawner, int min, int max) {
		setMobSpawnerDelay(spawner, min + GlobalVariables.random.nextInt(max - min));
	}
	public static void resetMobSpawnerDelay(BaseSpawner spawner) {
		resetMobSpawnerDelay(spawner, 200, 800);
	}
}
