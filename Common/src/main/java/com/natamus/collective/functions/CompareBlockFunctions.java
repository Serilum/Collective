package com.natamus.collective.functions;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.*;

public class CompareBlockFunctions {
	@SuppressWarnings("deprecation")
	public static boolean blockIsInRegistryHolder(Block block, TagKey<Block> tagKey) {
		return block.builtInRegistryHolder().is(tagKey);
	}

	public static boolean isStoneBlock(Block block) {
		return blockIsInRegistryHolder(block, BlockTags.BASE_STONE_OVERWORLD);
	}
	
	public static boolean isNetherStoneBlock(Block block) {
		return blockIsInRegistryHolder(block, BlockTags.BASE_STONE_NETHER);
	}
	
	public static boolean isTreeLeaf(Block block, boolean withNetherVariants) {
		if (blockIsInRegistryHolder(block, BlockTags.LEAVES) || block instanceof LeavesBlock) {
			return true;
		}
		if (withNetherVariants) {
			if (block.equals(Blocks.NETHER_WART_BLOCK) || block.equals(Blocks.WARPED_WART_BLOCK) || block.equals(Blocks.SHROOMLIGHT)) {
				return true;
			}
		}
		return block instanceof BushBlock;
	}
	public static boolean isTreeLeaf(Block block) {
		return isTreeLeaf(block, true);
	}
	
	public static boolean isTreeLog(Block block) {
		return blockIsInRegistryHolder(block, BlockTags.LOGS) || block instanceof RotatedPillarBlock;
	}
	
	public static boolean isSapling(Block block) {
		return CompareItemFunctions.itemIsInRegistryHolder(block.asItem(), ItemTags.SAPLINGS) || block instanceof SaplingBlock;
	}
	
	public static boolean isDirtBlock(Block block) {
		return block.equals(Blocks.GRASS_BLOCK) || block.equals(Blocks.DIRT) || block.equals(Blocks.COARSE_DIRT) || block.equals(Blocks.PODZOL);
	}
	
	public static boolean isPortalBlock(Block block) {
		return block instanceof NetherPortalBlock || BlockFunctions.blockToReadableString(block).equals("portal placeholder");
	}
	
	public static boolean isAirOrOverwritableBlock(Block block) {
		return block.equals(Blocks.AIR) || (block instanceof BushBlock) || (block instanceof SnowLayerBlock);
	}
}
