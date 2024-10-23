package com.natamus.collective.services.helpers;

import net.minecraft.world.item.ItemStack;

public interface ToolFunctionsHelper {
    boolean isTool(ItemStack itemstack);
    boolean isSword(ItemStack itemStack);
    boolean isShield(ItemStack itemStack);
    boolean isPickaxe(ItemStack itemStack);
    boolean isAxe(ItemStack itemStack);
    boolean isShovel(ItemStack itemStack);
    boolean isHoe(ItemStack itemStack);
    boolean isShears(ItemStack itemStack);
    boolean isFlintAndSteel(ItemStack itemStack);
}