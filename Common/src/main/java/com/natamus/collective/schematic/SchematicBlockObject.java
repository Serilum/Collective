package com.natamus.collective.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SchematicBlockObject {

	private final BlockPos position;
	private final BlockState state;
	
	public SchematicBlockObject(BlockPos position, BlockState state) {
		this.position = position;
		this.state = state;
	}
	
	public BlockPos getPosition() {
		return position;
	}
	
	public BlockState getState() {
		return state;
	}
	
	public BlockPos getPositionWithOfsset(int x, int y, int z) {
		return new BlockPos(x + position.getX(), y + position.getY(), z + position.getZ());
	}
}
