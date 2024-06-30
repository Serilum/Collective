package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FabricTeleportHelper implements TeleportHelper {
    @Override
    public boolean teleportEntity(Entity entity, ServerLevel serverLevel, Vec3 vec3) {
        return entity.teleportTo(serverLevel, vec3.x, vec3.y, vec3.z, RelativeMovement.ALL, entity.getYRot(), entity.getXRot());
    }

    @Override
    public boolean teleportEntity(Entity entity, ServerLevel serverLevel, BlockPos blockPos) {
        return entity.teleportTo(serverLevel, blockPos.getX()+0.5, blockPos.getY(), blockPos.getZ()+0.5, null, entity.getYRot(), entity.getXRot());
    }

    @Override
    public boolean teleportEntity(Entity entity, ResourceKey<Level> targetDimension, Vec3 vec3) {
        if (entity.level().isClientSide) {
            return false;
        }

        return teleportEntity(entity, entity.getServer().getLevel(targetDimension), vec3);
    }

    @Override
    public boolean teleportEntity(Entity entity, ResourceKey<Level> targetDimension, BlockPos blockPos) {
        if (entity.level().isClientSide) {
            return false;
        }

        return teleportEntity(entity, entity.getServer().getLevel(targetDimension), blockPos);
    }
}
