package com.natamus.collective.neoforge.services;

import com.natamus.collective.services.helpers.EventTriggerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;

public class NeoForgeEventTriggerHelper implements EventTriggerHelper {
	@Override
	public void triggerNetherPortalSpawnEvent(Level level, BlockPos portalPos, PortalShape size) {
			NeoForge.EVENT_BUS.post(new BlockEvent.PortalSpawnEvent(level, portalPos, level.getBlockState(portalPos), size));
	}

	@Override
	public void triggerLivingConversionEvent(LivingEntity entity, LivingEntity outcome) {
		EventHooks.onLivingConvert(entity, outcome);
	}

	@Override
	public boolean triggerBlockBreakEvent(Level level, Player player, BlockPos pos, BlockState state) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return true;
		}
		return !CommonHooks.fireBlockBreak(level, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos, state).isCanceled();
	}

	@Override
	public void triggerAfterBlockBreakEvent(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {

	}
}