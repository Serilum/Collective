package com.natamus.collective.services.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public interface TeleportHelper {
    boolean teleportEntity(Entity entity, ServerLevel serverLevel, BlockPos targetPos);
}