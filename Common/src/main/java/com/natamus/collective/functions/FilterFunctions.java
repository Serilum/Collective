package com.natamus.collective.functions;

import com.natamus.collective.services.Services;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class FilterFunctions {
	public static boolean isInFilter(String filter, Object value) {
		if (value instanceof Item item) {
			return isInItemFilter(filter, item);
		}
		if (value instanceof Block block) {
			return isInBlockFilter(filter, block);
		}
		if (value instanceof EntityType<?> entityType) {
			return isInEntityTypeFilter(filter, entityType);
		}
		return false;
	}

	public static boolean isInItemFilter(String filter, Item item) {
		ItemStack itemStack = new ItemStack(item);
		return switch (filter) {
			case "tools" -> Services.TOOLFUNCTIONS.isTool(itemStack) || Services.TOOLFUNCTIONS.isSword(itemStack) || (itemStack.isDamageableItem() && !(item instanceof Equipable));
			case "axes" -> Services.TOOLFUNCTIONS.isAxe(itemStack);
			case "pickaxes" -> Services.TOOLFUNCTIONS.isPickaxe(itemStack);
			case "shovels" -> Services.TOOLFUNCTIONS.isShovel(itemStack);
			case "hoes" -> Services.TOOLFUNCTIONS.isHoe(itemStack);
			case "swords" -> Services.TOOLFUNCTIONS.isSword(itemStack);
			case "blocks" -> item instanceof BlockItem;
			default -> false;
		};
	}

	public static boolean isInBlockFilter(String filter, Block block) {
		return switch (filter) {
			case "logs" -> block.defaultBlockState().is(BlockTags.LOGS);
			case "leaves" -> block.defaultBlockState().is(BlockTags.LEAVES);
			case "ores" -> Services.BLOCKTAGS.isOre(block.defaultBlockState(), true);
			default -> false;
		};
	}

	public static boolean isInEntityTypeFilter(String filter, EntityType<?> entityType) {
		MobCategory category = entityType.getCategory();
		return switch (filter) {
			case "mobs" -> category != MobCategory.MISC;
			case "monsters" -> category == MobCategory.MONSTER;
			case "animals" -> category != MobCategory.MISC && category.isFriendly();
			default -> false;
		};
	}
}
