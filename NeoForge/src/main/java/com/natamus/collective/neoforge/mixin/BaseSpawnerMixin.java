package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.util.CollectiveReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(value = BaseSpawner.class, priority = 1001)
public abstract class BaseSpawnerMixin {
    @Inject(method = "serverTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V", at = @At(value= "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void BaseSpawner_serverTick(ServerLevel p_151312_, BlockPos p_151313_, CallbackInfo ci, boolean flag, RandomSource randomsource, SpawnData spawndata, int i, ProblemReporter.ScopedCollector problemreporter$scopedcollector, ValueInput valueinput, Optional<?> optional, Vec3 vec3, BlockPos blockpos, Entity entity) {
		if (entity instanceof Mob) {
			entity.addTag(CollectiveReference.MOD_ID + ".fromspawner");
		}
	}
}
