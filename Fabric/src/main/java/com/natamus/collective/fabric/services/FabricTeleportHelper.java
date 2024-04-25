package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.TeleportHelper;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;

public class FabricTeleportHelper implements TeleportHelper {
    @Override
    public <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, PortalInfo portalInfo) {
        return FabricDimensions.teleport(entity, serverLevel, portalInfo);
    }
}
