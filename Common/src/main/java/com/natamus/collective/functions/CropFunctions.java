package com.natamus.collective.functions;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collections;

public class CropFunctions {
	public static boolean applyBonemeal(ItemStack itemstack, Level world, BlockPos pos, Player player) {
		BlockState blockstate = world.getBlockState(pos);
		
		if (blockstate.getBlock() instanceof BonemealableBlock) {
			BonemealableBlock bonemealableblock = (BonemealableBlock)blockstate.getBlock();
			if (bonemealableblock.isValidBonemealTarget(world, pos, blockstate)) {
				if (world instanceof ServerLevel) {
					if (bonemealableblock.isBonemealSuccess(world, world.random, pos, blockstate)) {
						bonemealableblock.performBonemeal((ServerLevel)world, world.random, pos, blockstate);
					}
					
					if (!player.isCreative()) {
						itemstack.shrink(1);
					}
				}
				
				return true;
			}
		}
		
		return false;
	}

	public static boolean growCrop(Level level, Player player, BlockState blockState, BlockPos blockPos) {
		return growCrop(level, player, blockState, blockPos, player.getItemInHand(InteractionHand.MAIN_HAND));
	}
	public static boolean growCrop(Level level, Player player, BlockState blockState, BlockPos blockPos, InteractionHand interactionHand) {
		return growCrop(level, player, blockState, blockPos, player.getItemInHand(interactionHand));
	}
	public static boolean growCrop(Level level, Player player, BlockState blockState, BlockPos blockPos, ItemStack itemStack) {
		if (level.isClientSide) {
			return false;
		}

		Block block = blockState.getBlock();

		if (block instanceof BonemealableBlock) {
			BonemealableBlock igrowable = (BonemealableBlock)block;
			while (igrowable.isValidBonemealTarget(level, blockPos, blockState)) {
				if (!igrowable.isBonemealSuccess(level, level.random, blockPos, blockState)) {
					break;
				}
				igrowable.performBonemeal((ServerLevel)level, level.random, blockPos, blockState);
				blockState = level.getBlockState(blockPos);
				itemStack.shrink(1);
				if (itemStack.getCount() == 0) {
					break;
				}
			}
		}
		else {
			for (Property<?> property : Collections.unmodifiableCollection(blockState.getValues().keySet())) {
				if (property instanceof IntegerProperty) {
					IntegerProperty prop = (IntegerProperty) property;
					String name = prop.getName();
					if (name.equals("age")) {
						Comparable<?> cv = blockState.getValues().get(property);
						int value = Integer.parseUnsignedInt(cv.toString());
						int max = Collections.max(prop.getPossibleValues());
						if (value == max) {
							return false;
						}

						while (value < max) {
							level.setBlockAndUpdate(blockPos, level.getBlockState(blockPos).cycle(property));
							if (!player.isCreative()) {
								itemStack.shrink(1);
								if (itemStack.getCount() == 0) {
									break;
								}
							}
							value += 1;

							if (!player.isCrouching()) {
								break;
							}
						}
					}
				}
			}
		}
		
		level.levelEvent(2005, blockPos, 0);
		return true;
	}
	
	public static boolean growCactus(Level world, BlockPos pos) {
		int height = world.getHeight();
		for (int y = pos.getY(); y <= height; y++) {
			BlockPos uppos = new BlockPos(pos.getX(), y, pos.getZ());
			Block block = world.getBlockState(uppos).getBlock();
			if (block != Blocks.CACTUS) {
				if (block.equals(Blocks.AIR)) {
					world.setBlockAndUpdate(uppos, Blocks.CACTUS.defaultBlockState());
					world.levelEvent(2005, uppos, 0);
					world.levelEvent(2005, uppos.above(), 0);
					return true;
				}
				break;
			}
		}
		return false;
	}
	
	public static boolean growSugarcane(Level world, BlockPos pos) {
		int height = world.getHeight();
		for (int y = pos.getY(); y <= height; y++) {
			BlockPos uppos = new BlockPos(pos.getX(), y, pos.getZ());
			Block block = world.getBlockState(uppos).getBlock();
			if (block != Blocks.SUGAR_CANE) {
				if (block.equals(Blocks.AIR)) {
					world.setBlockAndUpdate(uppos, Blocks.SUGAR_CANE.defaultBlockState());
					world.levelEvent(2005, uppos, 0);
					world.levelEvent(2005, uppos.above(), 0);
					return true;
				}
				break;
			}
		}
		return false;
	}
	
	public static boolean growVine(Level world, BlockPos pos) {
		for (int y = pos.getY(); y > 0; y--) {
			BlockPos downpos = new BlockPos(pos.getX(), y, pos.getZ());
			Block block = world.getBlockState(downpos).getBlock();
			if (block != Blocks.VINE) {
				if (block.equals(Blocks.AIR)) {
					world.setBlockAndUpdate(downpos, world.getBlockState(pos));
					world.levelEvent(2005, downpos, 0);
					world.levelEvent(2005, downpos.below(), 0);
					return true;
				}
				break;
			}
		}
		return false;
	}
}
