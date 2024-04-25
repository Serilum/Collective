package com.natamus.collective.services.helpers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;

public interface TeleportHelper {
    <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, PortalInfo portalInfo);
}