package com.natamus.collective.forge.services;

import com.natamus.collective.services.helpers.BlockTagsHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

public class ForgeBlockTagsHelper implements BlockTagsHelper {
    @Override
	public boolean isOre(BlockState blockState) {
		return isOre(blockState, false);
    }

    @Override
	public boolean isOre(BlockState blockState, boolean fuzzyCheck) {
		if (fuzzyCheck) {
			String rawName = blockState.getBlock().getName().toString();
			if (rawName.contains("_ore'") || rawName.contains("_ore_")) {
				return true;
			}
		}
		return blockState.is(Tags.Blocks.ORES);
    }
}