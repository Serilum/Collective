package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.ToolFunctionsHelper;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.world.item.*;

public class FabricToolFunctionsHelper implements ToolFunctionsHelper {
    @Override
    public boolean isTool(ItemStack itemstack) {
		return isPickaxe(itemstack) || isAxe(itemstack) || isShovel(itemstack) || isHoe(itemstack) || isShears(itemstack);
    }

    @Override
	public boolean isSword(ItemStack itemStack) {
        return itemStack.getItem() instanceof SwordItem || itemStack.is(ConventionalItemTags.SWORDS);
    }

    @Override
	public boolean isShield(ItemStack itemStack) {
		return itemStack.getItem() instanceof ShieldItem || itemStack.is(ConventionalItemTags.SHIELDS);
	}

    @Override
	public boolean isPickaxe(ItemStack itemStack) {
        return itemStack.getItem() instanceof PickaxeItem || itemStack.is(ConventionalItemTags.PICKAXES);
    }

    @Override
	public boolean isAxe(ItemStack itemStack) {
        return itemStack.getItem() instanceof AxeItem || itemStack.is(ConventionalItemTags.AXES);
    }

    @Override
	public boolean isShovel(ItemStack itemStack) {
        return itemStack.getItem() instanceof ShovelItem || itemStack.is(ConventionalItemTags.SHOVELS);
    }

    @Override
	public boolean isHoe(ItemStack itemStack) {
        return itemStack.getItem() instanceof HoeItem || itemStack.is(ConventionalItemTags.HOES);
    }

    @Override
	public boolean isShears(ItemStack itemStack) {
		return itemStack.getItem() instanceof ShearsItem || itemStack.is(ConventionalItemTags.SHEARS);
	}

    @Override
    public boolean isFlintAndSteel(ItemStack itemStack) {
        return itemStack.getItem() instanceof FlintAndSteelItem;
    }
}
