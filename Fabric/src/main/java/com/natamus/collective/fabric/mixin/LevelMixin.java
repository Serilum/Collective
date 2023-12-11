package com.natamus.collective.fabric.mixin;

import java.util.EnumSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.natamus.collective.fabric.callbacks.CollectiveBlockEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

@Mixin(value = Level.class, priority = 1001)
public class LevelMixin {
	@Inject(method = "updateNeighborsAt", at = @At(value = "HEAD"), cancellable = true) 
	public void Level_updateNeighborsAt(BlockPos blockPos, Block block, CallbackInfo ci) {
		Level level = (Level)(Object)this;
		if (!CollectiveBlockEvents.NEIGHBOUR_NOTIFY.invoker().onNeighbourNotify(level, blockPos, level.getBlockState(blockPos), EnumSet.allOf(Direction.class), false)) {
			ci.cancel();
		}
	}
	
	@Inject(method = "updateNeighborsAtExceptFromFacing", at = @At(value = "HEAD"), cancellable = true) 
	public void Level_updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, Direction direction, CallbackInfo ci) {
		Level level = (Level)(Object)this;
		EnumSet<Direction> directions = EnumSet.allOf(Direction.class);
		directions.remove(direction);
		if (!CollectiveBlockEvents.NEIGHBOUR_NOTIFY.invoker().onNeighbourNotify(level, blockPos, level.getBlockState(blockPos), directions, false)) {
			ci.cancel();
		}
	}
}
