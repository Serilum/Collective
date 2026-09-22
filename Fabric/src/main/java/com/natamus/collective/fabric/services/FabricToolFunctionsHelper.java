package com.natamus.collective.fabric.services;

import com.natamus.collective.functions.ItemFunctions;
import com.natamus.collective.services.helpers.ToolFunctionsHelper;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlockTransformers;

public class FabricToolFunctionsHelper implements ToolFunctionsHelper {
	@Override
	public boolean isTool(ItemStack itemstack) {
		return isPickaxe(itemstack) || isAxe(itemstack) || isShovel(itemstack) || isHoe(itemstack) || isShears(itemstack);
	}

	@Override
	public boolean isSword(ItemStack itemStack) {
		return itemStack.is(ItemTags.SWORDS);
	}

	@Override
	public boolean isShield(ItemStack itemStack) {
		return itemStack.getItem() instanceof ShieldItem || itemStack.is(ConventionalItemTags.SHIELD_TOOLS);
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
		return itemStack.getItem() instanceof ShearsItem || itemStack.is(ConventionalItemTags.SHEAR_TOOLS);
	}

	@Override
	public boolean isFlintAndSteel(ItemStack itemStack) {
		return itemStack.getItem() instanceof FlintAndSteelItem;
	}
}
