package com.natamus.collective.services.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface TeleportHelper {
    boolean teleportEntity(Entity entity, ServerLevel serverLevel, Vec3 vec3);
    boolean teleportEntity(Entity entity, ServerLevel serverLevel, BlockPos blockPos);
    boolean teleportEntity(Entity entity, ResourceKey<Level> targetDimension, Vec3 vec3);
    boolean teleportEntity(Entity entity, ResourceKey<Level> targetDimension, BlockPos blockPos);
}