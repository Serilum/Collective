package com.natamus.collective.neoforge.services;

import com.natamus.collective.services.helpers.ToolFunctionsHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Set;

public class NeoForgeToolFunctionsHelper implements ToolFunctionsHelper {
    @Override
	public boolean isTool(ItemStack itemstack) {
		return isPickaxe(itemstack) || isAxe(itemstack) || isShovel(itemstack) || isHoe(itemstack) || isShears(itemstack);
    }

    @Override
	public boolean isSword(ItemStack itemStack) {
        return itemStack.getItem() instanceof SwordItem || itemStack.is(ItemTags.SWORDS) || canPerformOneOfActions(itemStack, ItemAbilities.DEFAULT_SWORD_ACTIONS);
    }

    @Override
	public boolean isShield(ItemStack itemStack) {
		return itemStack.getItem() instanceof ShieldItem || canPerformOneOfActions(itemStack, ItemAbilities.DEFAULT_SHIELD_ACTIONS);
	}

    @Override
	public boolean isPickaxe(ItemStack itemStack) {
        return itemStack.getItem() instanceof PickaxeItem || itemStack.is(ItemTags.PICKAXES) || canPerformOneOfActions(itemStack, ItemAbilities.DEFAULT_PICKAXE_ACTIONS);
    }

    @Override
	public boolean isAxe(ItemStack itemStack) {
        return itemStack.getItem() instanceof AxeItem || itemStack.is(ItemTags.AXES) || canPerformOneOfActions(itemStack, ItemAbilities.DEFAULT_AXE_ACTIONS);
    }

    @Override
	public boolean isShovel(ItemStack itemStack) {
        return itemStack.getItem() instanceof ShovelItem || itemStack.is(ItemTags.SHOVELS) || canPerformOneOfActions(itemStack, ItemAbilities.DEFAULT_SHOVEL_ACTIONS);
    }

    @Override
	public boolean isHoe(ItemStack itemStack) {
        return itemStack.getItem() instanceof HoeItem || itemStack.is(ItemTags.HOES) || canPerformOneOfActions(itemStack, ItemAbilities.DEFAULT_HOE_ACTIONS);
    }

    @Override
	public boolean isShears(ItemStack itemStack) {
		return itemStack.getItem() instanceof ShearsItem || canPerformOneOfActions(itemStack, ItemAbilities.DEFAULT_SHEARS_ACTIONS);
	}

    public static final ItemAbility LIGHT_CAMPFIRE = ItemAbility.get("light_campfire");
    @Override public boolean isFlintAndSteel(ItemStack itemStack) {
        return itemStack.getItem() instanceof FlintAndSteelItem || itemStack.canPerformAction(LIGHT_CAMPFIRE);
    }

	private static boolean canPerformOneOfActions(ItemStack itemStack, Set<ItemAbility> itemAbilities) {
		for (ItemAbility itemAbility : itemAbilities) {
			if (itemStack.canPerformAction(itemAbility)) {
				return true;
			}
		}
		return false;
	}
}