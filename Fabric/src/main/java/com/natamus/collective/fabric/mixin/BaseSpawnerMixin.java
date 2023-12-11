package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveSpawnEvents;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(value = BaseSpawner.class, priority = 1001)
public abstract class BaseSpawnerMixin {
	@Shadow protected abstract void delay(Level level, BlockPos blockPos);
	
	@Inject(method = "serverTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V", at = @At(value= "INVOKE", target = "Lnet/minecraft/world/entity/Mob;checkSpawnObstruction(Lnet/minecraft/world/level/LevelReader;)Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
	private void BaseSpawner_serverTickprivate(ServerLevel serverLevel, BlockPos blockPos, CallbackInfo ci, boolean bl, int i, CompoundTag compoundTag, Optional optional, ListTag listTag, int j, RandomSource randomSource, double d, double e, double f, BlockPos blockPos2, Entity entity, int k, Mob mob) {
		mob.addTag(CollectiveReference.MOD_ID + ".fromspawner");
		
		if (!CollectiveSpawnEvents.MOB_CHECK_SPAWN.invoker().onMobCheckSpawn(mob, serverLevel, blockPos, MobSpawnType.SPAWNER)) {
			ci.cancel();
			delay(serverLevel, blockPos);
		}
	}
}
