package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveBlockEvents;
import com.natamus.collective.fabric.callbacks.CollectiveEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumSet;

@Mixin(value = ServerLevel.class, priority = 1001)
public class ServerLevelMixin {
	@Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "HEAD"), cancellable = true)
	private void serverLevel_addEntity(Entity entity, CallbackInfoReturnable<Boolean> ci) {
		Level world = entity.getCommandSenderWorld();
		if (!CollectiveEntityEvents.PRE_ENTITY_JOIN_WORLD.invoker().onPreSpawn(world, entity)) {
			ci.setReturnValue(false);
		}
	}

	@Inject(method = "updateNeighborsAt", at = @At(value = "HEAD"), cancellable = true)
	public void Level_updateNeighborsAt(BlockPos blockPos, Block block, CallbackInfo ci) {
		ServerLevel serverLevel = (ServerLevel)(Object)this;
		if (!CollectiveBlockEvents.NEIGHBOUR_NOTIFY.invoker().onNeighbourNotify(serverLevel, blockPos, serverLevel.getBlockState(blockPos), EnumSet.allOf(Direction.class), false)) {
			ci.cancel();
		}
	}

	@Inject(method = "updateNeighborsAtExceptFromFacing", at = @At(value = "HEAD"), cancellable = true)
	public void Level_updateNeighborsAtExceptFromFacing(BlockPos blockPos, Block block, Direction direction, CallbackInfo ci) {
		ServerLevel serverLevel = (ServerLevel)(Object)this;
		EnumSet<Direction> directions = EnumSet.allOf(Direction.class);
		directions.remove(direction);
		if (!CollectiveBlockEvents.NEIGHBOUR_NOTIFY.invoker().onNeighbourNotify(serverLevel, blockPos, serverLevel.getBlockState(blockPos), directions, false)) {
			ci.cancel();
		}
	}
}
