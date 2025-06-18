package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveSpawnEvents;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
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
	private void BaseSpawner_serverTickprivate(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci, boolean bl, RandomSource randomSource, SpawnData spawnData, int i, ProblemReporter.ScopedCollector scopedCollector, ValueInput valueInput, Optional<?> optional, Vec3 vec3, BlockPos blockPos, Entity entity, int j, Mob mob) {
		mob.addTag(CollectiveReference.MOD_ID + ".fromspawner");
		
		if (!CollectiveSpawnEvents.MOB_CHECK_SPAWN.invoker().onMobCheckSpawn(mob, serverLevel, blockPos, EntitySpawnReason.SPAWNER)) {
			ci.cancel();
			delay(serverLevel, blockPos);
		}
	}
}
