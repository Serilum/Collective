package com.natamus.collective.fabric.mixin;

import com.natamus.collective.events.CollectiveSpawnCheckEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetherPortalBlock.class, priority = 1001)
public class NetherPortalBlockMixin {
	@Inject(method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"), cancellable = true)
	private void randomTick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource random, CallbackInfo ci) {
		if (!CollectiveSpawnCheckEvents.onNetherPortalSpawn(serverLevel, pos.above())) {
			ci.cancel();
		}
	}
}
