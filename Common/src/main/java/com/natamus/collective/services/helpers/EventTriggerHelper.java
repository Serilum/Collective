package com.natamus.collective.services.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalShape;

public interface EventTriggerHelper {
    void triggerNetherPortalSpawnEvent(Level level, BlockPos portalPos, PortalShape size);
}