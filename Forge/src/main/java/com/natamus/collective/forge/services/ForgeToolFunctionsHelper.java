package com.natamus.collective.forge.services;

import com.natamus.collective.functions.ItemFunctions;
import com.natamus.collective.services.helpers.ToolFunctionsHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.Set;

public class ForgeToolFunctionsHelper implements ToolFunctionsHelper {
	@Override
	public boolean isTool(ItemStack itemstack) {
		return isPickaxe(itemstack) || isAxe(itemstack) || isShovel(itemstack) || isHoe(itemstack) || isShears(itemstack);
	}

	@Override
	public boolean isSword(ItemStack itemStack) {
		return itemStack.is(ItemTags.SWORDS) || itemStack.canPerformAction(ToolActions.SWORD_SWEEP);
	}

	@Override
	public boolean isShield(ItemStack itemStack) {
		return itemStack.getItem() instanceof ShieldItem;
	}

	@Override
	public boolean isPickaxe(ItemStack itemStack) {
		return itemStack.is(ItemTags.PICKAXES);
	}

	@Override
	public boolean isAxe(ItemStack itemStack) {
		return ItemFunctions.hasBlockTransformer(itemStack, BlockTransformers.AXE) || itemStack.is(ItemTags.AXES);
	}

	@Override
	public boolean isShovel(ItemStack itemStack) {
		return ItemFunctions.hasBlockTransformer(itemStack, BlockTransformers.SHOVEL) || itemStack.is(ItemTags.SHOVELS);
	}

	@Override
	public boolean isHoe(ItemStack itemStack) {
		return ItemFunctions.hasBlockTransformer(itemStack, BlockTransformers.HOE) || itemStack.is(ItemTags.HOES);
	}

	@Override
	public boolean isShears(ItemStack itemStack) {
		return itemStack.getItem() instanceof ShearsItem || canPerformOneOfActions(itemStack, ToolActions.DEFAULT_SHEARS_ACTIONS);
	}

	public static final ToolAction LIGHT_CAMPFIRE = ToolAction.get("light_campfire");
	@Override public boolean isFlintAndSteel(ItemStack itemStack) {
		return itemStack.getItem() instanceof FlintAndSteelItem || itemStack.canPerformAction(LIGHT_CAMPFIRE);
	}

	private static boolean canPerformOneOfActions(ItemStack itemStack, Set<ToolAction> toolActions) {
		for (ToolAction toolAction : toolActions) {
			if (itemStack.canPerformAction(toolAction)) {
				return true;
			}
		}
		return false;
	}
}