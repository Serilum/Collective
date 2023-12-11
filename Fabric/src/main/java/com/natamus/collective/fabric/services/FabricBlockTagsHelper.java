package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.BlockTagsHelper;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.world.level.block.state.BlockState;

public class FabricBlockTagsHelper implements BlockTagsHelper {
    @Override
    public boolean isOre(BlockState blockState) {
		return blockState.is(ConventionalBlockTags.ORES);
    }
}
