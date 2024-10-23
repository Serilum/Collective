package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveSpawnEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = NaturalSpawner.class, priority = 1001)
public class NaturalSpawnerMixin {
	@ModifyVariable(method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V", at = @At(value= "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/NaturalSpawner;getMobForSpawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/EntityType;)Lnet/minecraft/world/entity/Mob;", ordinal = 0))
	private static Mob NaturalSpawner_spawnCategoryForPosition(Mob mob, MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback) {
		if (mob != null) {
			mob.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		}
		
		if (!CollectiveSpawnEvents.MOB_CHECK_SPAWN.invoker().onMobCheckSpawn(mob, serverLevel, null, EntitySpawnReason.NATURAL)) {
			mob = null;
		}
		
		return mob;
	}
}
