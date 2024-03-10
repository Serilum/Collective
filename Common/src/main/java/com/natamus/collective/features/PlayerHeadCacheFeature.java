package com.natamus.collective.features;

import com.natamus.collective.data.FeatureFlags;
import com.natamus.collective.functions.HeadFunctions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

public class PlayerHeadCacheFeature {
	public static HashMap<String, ItemStack> cachedPlayerHeadsMap = new HashMap<String, ItemStack>();

	public static ItemStack cachePlayer(Player player) {
		return cachePlayer(player.getName().getString());
	}
	public static ItemStack cachePlayer(String playerName) {
		if (cachedPlayerHeadsMap.containsKey(playerName)) {
			return cachedPlayerHeadsMap.get(playerName).copy();
		}

		ItemStack headStack = HeadFunctions.getPlayerHead(playerName, 1);
		if (headStack == null) {
			return null;
		}

		cachedPlayerHeadsMap.put(playerName, headStack);

		return headStack.copy();
	}

	public static ItemStack getPlayerHeadStackFromCache(Player player) {
		return getPlayerHeadStackFromCache(player.getName().getString());
	}
	public static ItemStack getPlayerHeadStackFromCache(String playerName) {
		if (cachedPlayerHeadsMap.containsKey(playerName)) {
			return cachedPlayerHeadsMap.get(playerName).copy();
		}

		return cachePlayer(playerName);
	}

	public static boolean isHeadCachingEnabled() {
		return FeatureFlags.shouldCachePlayerHeads;
	}

	public static void enableHeadCaching() {
		FeatureFlags.shouldCachePlayerHeads = true;
	}

	public static boolean resetPlayerHeadCache() {
		cachedPlayerHeadsMap = new HashMap<String, ItemStack>();
		return true;
	}
}
