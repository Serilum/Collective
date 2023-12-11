package com.natamus.collective.fabric.services;

import com.natamus.collective.fabric.callbacks.CollectiveBlockEvents;
import com.natamus.collective.services.helpers.EventTriggerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalShape;

public class FabricEventTriggerHelper implements EventTriggerHelper {
    @Override
    public void triggerNetherPortalSpawnEvent(Level level, BlockPos portalPos, PortalShape size) {
        CollectiveBlockEvents.ON_NETHER_PORTAL_SPAWN.invoker().onPossiblePortal(level, portalPos, size);
    }
}