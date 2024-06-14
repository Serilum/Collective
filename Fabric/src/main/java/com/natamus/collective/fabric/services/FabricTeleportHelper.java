package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.TeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class FabricTeleportHelper implements TeleportHelper {
    @Override
    public boolean teleportEntity(Entity entity, ServerLevel serverLevel, BlockPos targetPos) {
        return entity.teleportTo(serverLevel, targetPos.getX(), targetPos.getY(), targetPos.getZ(), null, entity.getYRot(), entity.getXRot());
    }
}
