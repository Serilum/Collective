package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.events.CollectiveSpawnCheckEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PatrolSpawner.class, priority = 1001)
public class PatrolSpawnerMixin {
	@Inject(method = "spawnPatrolMember(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;"), cancellable = true)
	private void spawnPatrolMember(ServerLevel serverLevel, BlockPos pos, RandomSource random, boolean isLeader, CallbackInfoReturnable<Boolean> cir) {
		if (!CollectiveSpawnCheckEvents.onPatrolMemberSpawn(serverLevel, pos)) {
			cir.setReturnValue(false);
		}
	}
}
