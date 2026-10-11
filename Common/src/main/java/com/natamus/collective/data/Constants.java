package com.natamus.collective.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Constants {
	public static final Logger LOG = LoggerFactory.getLogger(CollectiveReference.NAME);
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static final List<EquipmentSlot> equipmentSlots = Arrays.asList(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFFHAND);

	public static ItemStack normalPickaxeStack = null;
	public static ItemStack silkPickaxeStack = null;

	private static boolean ranInit = false;
	public static void initConstantData() {
		if (ranInit) {
			return;
		}

		normalPickaxeStack = new ItemStack(Items.NETHERITE_PICKAXE);
		silkPickaxeStack = new ItemStack(Items.NETHERITE_PICKAXE);
		silkPickaxeStack.enchant(Enchantments.SILK_TOUCH, 1);

		ranInit = true;
	}
}
