package com.natamus.collective.neoforge.services;

import com.natamus.collective.services.helpers.EventTriggerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalShape;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public class NeoForgeEventTriggerHelper implements EventTriggerHelper {
    @Override
    public void triggerNetherPortalSpawnEvent(Level level, BlockPos portalPos, PortalShape size) {
	        NeoForge.EVENT_BUS.post(new BlockEvent.PortalSpawnEvent(level, portalPos, level.getBlockState(portalPos), size));
    }
}