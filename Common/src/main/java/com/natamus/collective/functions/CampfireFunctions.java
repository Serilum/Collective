package com.natamus.collective.functions;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CampfireFunctions {
	public static boolean isCampfire(BlockState state) {
		return state.getBlock() instanceof CampfireBlock || state.is(BlockTags.CAMPFIRES);
	}

	public static boolean isSoulCampfire(BlockState campfireState) {
		return campfireState.getBlock().getDescriptionId().contains("soul");
	}

	public static boolean isLit(BlockState campfireState) {
		if (!campfireState.hasProperty(CampfireBlock.LIT)) {
			return true;
		}

		return campfireState.getValue(CampfireBlock.LIT);
	}

	public static boolean isSignalling(BlockState campfireState) {
		if (!campfireState.hasProperty(CampfireBlock.SIGNAL_FIRE)) {
			return false;
		}

		return campfireState.getValue(CampfireBlock.SIGNAL_FIRE);
	}
}
