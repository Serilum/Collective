package com.natamus.collective.services.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;

public interface TeleportHelper {
    <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, PortalInfo portalInfo);
    <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, Vec3 vec3);
    <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, BlockPos blockPos);
    <T extends Entity> Entity teleportEntity(T entity, ResourceKey<Level> targetDimension, Vec3 vec3);
    <T extends Entity> Entity teleportEntity(T entity, ResourceKey<Level> targetDimension, BlockPos blockPos);
}