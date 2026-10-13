package com.natamus.collective.forge.services;

import com.natamus.collective.services.helpers.EventTriggerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;

public class ForgeEventTriggerHelper implements EventTriggerHelper {
	@Override
	public void triggerNetherPortalSpawnEvent(Level level, BlockPos portalPos, PortalShape size) {
		BlockEvent.PortalSpawnEvent.BUS.post(new BlockEvent.PortalSpawnEvent(level, portalPos, level.getBlockState(portalPos), size));
	}

	@Override
	public void triggerLivingConversionEvent(LivingEntity entity, LivingEntity outcome) {
		ForgeEventFactory.onLivingConvert(entity, outcome);
	}

	@Override
	public boolean triggerBlockBreakEvent(Level level, Player player, BlockPos pos, BlockState state) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return true;
		}
		return ForgeHooks.onBlockBreakEvent(level, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos) != -1;
	}

	@Override
	public void triggerAfterBlockBreakEvent(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {

	}
}