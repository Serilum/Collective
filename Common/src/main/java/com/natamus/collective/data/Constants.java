package com.natamus.collective.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final Logger LOG = LoggerFactory.getLogger(CollectiveReference.NAME);
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final List<EquipmentSlot> equipmentSlots = Arrays.asList(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFFHAND);

    public static final ItemStack normalPickaxeStack = new ItemStack(Items.NETHERITE_PICKAXE);
    public static final ItemStack silkPickaxeStack = new ItemStack(Items.NETHERITE_PICKAXE);

	private static boolean ranInit = false;
    public static void initConstantData(Level level) {
		if (ranInit) {
			return;
		}

		ItemEnchantments.Mutable itemEnchantmentsMutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		itemEnchantmentsMutable.set(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
		EnchantmentHelper.setEnchantments(silkPickaxeStack, itemEnchantmentsMutable.toImmutable());

		ranInit = true;
    }
}
