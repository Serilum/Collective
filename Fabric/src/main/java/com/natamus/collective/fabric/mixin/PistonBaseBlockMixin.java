package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectivePistonEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PistonBaseBlock.class, priority = 1001)
public class PistonBaseBlockMixin {
	@Inject(method = "triggerEvent(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/piston/PistonBaseBlock;moveBlocks(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z", ordinal = 0), cancellable = true)
	public void triggerEvent_default(BlockState blockState, Level level, BlockPos blockPos, int i, int j, CallbackInfoReturnable<Boolean> cir) {
		if (!CollectivePistonEvents.PRE_PISTON_ACTIVATE.invoker().onPistonActivate(level, blockPos, blockState.getValue(DirectionalBlock.FACING), false)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "triggerEvent(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;", ordinal = 0), cancellable = true)
	public void triggerEvent_extended(BlockState blockState, Level level, BlockPos blockPos, int i, int j, CallbackInfoReturnable<Boolean> cir) {
		if (!CollectivePistonEvents.PRE_PISTON_ACTIVATE.invoker().onPistonActivate(level, blockPos, blockState.getValue(DirectionalBlock.FACING), true)) {
			cir.setReturnValue(false);
		}
	}
}
