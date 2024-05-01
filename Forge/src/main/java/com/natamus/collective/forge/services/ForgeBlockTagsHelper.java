package com.natamus.collective.forge.services;

import com.natamus.collective.services.helpers.BlockTagsHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

public class ForgeBlockTagsHelper implements BlockTagsHelper {
    @Override
	public boolean isOre(BlockState blockState) {
		return blockState.is(Tags.Blocks.ORES);
    }
}