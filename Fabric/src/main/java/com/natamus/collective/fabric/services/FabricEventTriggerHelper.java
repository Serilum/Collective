package com.natamus.collective.fabric.services;

import com.natamus.collective.fabric.callbacks.CollectiveBlockEvents;
import com.natamus.collective.services.helpers.EventTriggerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class FabricEventTriggerHelper implements EventTriggerHelper {
	@Override
	public void triggerNetherPortalSpawnEvent(Level level, BlockPos portalPos, PortalShape size) {
		CollectiveBlockEvents.ON_NETHER_PORTAL_SPAWN.invoker().onPossiblePortal(level, portalPos, size);
	}

	@Override
	public void triggerLivingConversionEvent(LivingEntity entity, LivingEntity outcome) {

	}

	@Override
	public boolean triggerBlockBreakEvent(Level level, Player player, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		boolean isAllowed = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(level, player, pos, state, blockEntity);
		if (!isAllowed) {
			PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(level, player, pos, state, blockEntity);
		}
		return isAllowed;
	}

	@Override
	public void triggerAfterBlockBreakEvent(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(level, player, pos, state, blockEntity);
	}
}