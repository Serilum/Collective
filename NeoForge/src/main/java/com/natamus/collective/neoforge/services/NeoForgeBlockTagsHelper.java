package com.natamus.collective.neoforge.services;

import com.natamus.collective.services.helpers.BlockTagsHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public class NeoForgeBlockTagsHelper implements BlockTagsHelper {
    @Override
	public boolean isOre(BlockState blockState) {
		return blockState.is(Tags.Blocks.ORES);
    }
}