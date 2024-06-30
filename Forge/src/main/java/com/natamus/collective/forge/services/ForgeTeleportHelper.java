package com.natamus.collective.forge.services;

import com.natamus.collective.services.helpers.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class ForgeTeleportHelper implements TeleportHelper {
    @Override
    public <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, PortalInfo portalInfo) {
        return entity.changeDimension(serverLevel, new SimpleTeleporter(portalInfo));
    }

    @Override
    public <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, Vec3 vec3) {
        return teleportEntity(entity, serverLevel, new PortalInfo(vec3, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot()));
    }

    @Override
    public <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, BlockPos blockPos) {
        return teleportEntity(entity, serverLevel, new Vec3(blockPos.getX()+0.5, blockPos.getY(), blockPos.getZ()+0.5));
    }

    @Override
    public <T extends Entity> Entity teleportEntity(T entity, ResourceKey<Level> targetDimension, Vec3 vec3) {
        if (entity.level().isClientSide) {
            return entity;
        }

        return teleportEntity(entity, entity.getServer().getLevel(targetDimension), new PortalInfo(vec3, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot()));
    }

    @Override
    public <T extends Entity> Entity teleportEntity(T entity, ResourceKey<Level> targetDimension, BlockPos blockPos) {
        return teleportEntity(entity, targetDimension, new Vec3(blockPos.getX()+0.5, blockPos.getY(), blockPos.getZ()+0.5));
    }

    public record SimpleTeleporter(PortalInfo portalInfo) implements ITeleporter {
        @Override
        public PortalInfo getPortalInfo(Entity entity, ServerLevel destinationServerLevel, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
            return portalInfo;
        }
    }
}