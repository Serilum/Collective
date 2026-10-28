package com.natamus.collective.services.helpers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;

public interface EventTriggerHelper {
	void triggerNetherPortalSpawnEvent(Level level, BlockPos portalPos, PortalShape size);
	void triggerLivingConversionEvent(LivingEntity entity, LivingEntity outcome);
	boolean triggerBlockBreakEvent(Level level, Player player, BlockPos pos, BlockState state);
	void triggerAfterBlockBreakEvent(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity);
}
