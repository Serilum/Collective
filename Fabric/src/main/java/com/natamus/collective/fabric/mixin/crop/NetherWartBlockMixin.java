package com.natamus.collective.fabric.mixin.crop;

import com.natamus.collective.fabric.callbacks.CollectiveCropEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetherWartBlock.class, priority = 1001)
public class NetherWartBlockMixin {
	@Inject(method = "randomTick", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"), cancellable = true)
	public void NetherWartBlock_randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
		if (!CollectiveCropEvents.PRE_CROP_GROW.invoker().onPreCropGrow(serverLevel, blockPos, blockState)) {
			ci.cancel();
		}
	}
}
