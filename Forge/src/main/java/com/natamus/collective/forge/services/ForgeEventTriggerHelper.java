package com.natamus.collective.forge.services;

import com.natamus.collective.services.helpers.EventTriggerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

public class ForgeEventTriggerHelper implements EventTriggerHelper {
    @Override
    public void triggerNetherPortalSpawnEvent(Level level, BlockPos portalPos, PortalShape size) {
	        MinecraftForge.EVENT_BUS.post(new BlockEvent.PortalSpawnEvent(level, portalPos, level.getBlockState(portalPos), size));
    }
}