package com.natamus.collective.data;

import net.minecraft.client.KeyMapping.Category;

import java.util.Map;

public class ClientConstants {
	public static final Map<String, Category> keyMappingCategories = Map.of(
		"key.categories.creative", Category.CREATIVE,
		"key.categories.gameplay", Category.GAMEPLAY,
		"key.categories.inventory", Category.INVENTORY,
		"key.categories.misc", Category.MISC,
		"key.categories.movement", Category.MOVEMENT,
		"key.categories.multiplayer", Category.MULTIPLAYER
	);
}
