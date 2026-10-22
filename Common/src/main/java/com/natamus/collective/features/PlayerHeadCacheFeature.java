package com.natamus.collective.features;

import com.mojang.authlib.GameProfile;
import com.natamus.collective.data.FeatureFlags;
import com.natamus.collective.data.GlobalCaches;
import com.natamus.collective.functions.HeadFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;

public class PlayerHeadCacheFeature {
	public static ItemStack cachePlayer(Player player) {
		return cachePlayer(player.getName().getString(), player.getGameProfile());
	}
	public static ItemStack cachePlayer(ServerLevel serverLevel, String playerName) {
		if (GlobalCaches.cachedPlayerHeadsMap.containsKey(playerName)) {
			return GlobalCaches.cachedPlayerHeadsMap.get(playerName).copy();
		}

		ItemStack headStack = HeadFunctions.getNewPlayerHead(serverLevel, playerName, 1);
		if (headStack == null) {
			return null;
		}

		GlobalCaches.cachedPlayerHeadsMap.put(playerName, headStack);

		return headStack.copy();
	}
	public static ItemStack cachePlayer(String playerName, GameProfile gameProfile) {
		if (GlobalCaches.cachedPlayerHeadsMap.containsKey(playerName)) {
			return GlobalCaches.cachedPlayerHeadsMap.get(playerName).copy();
		}

		ItemStack headStack = HeadFunctions.getNewPlayerHead(gameProfile, 1);
		if (headStack == null) {
			return null;
		}

		GlobalCaches.cachedPlayerHeadsMap.put(gameProfile.name(), headStack);

		return headStack.copy();
	}

	public static ItemStack getPlayerHeadStackFromCache(Player player) {
		return cachePlayer(player);
	}
	public static ItemStack getPlayerHeadStackFromCache(ServerLevel serverLevel, String playerName) {
		if (GlobalCaches.cachedPlayerHeadsMap.containsKey(playerName)) {
			return GlobalCaches.cachedPlayerHeadsMap.get(playerName).copy();
		}

		return cachePlayer(serverLevel, playerName);
	}

	public static boolean isHeadCachingEnabled() {
		return FeatureFlags.shouldCachePlayerHeads;
	}

	public static void enableHeadCaching() {
		FeatureFlags.shouldCachePlayerHeads = true;
	}

	public static boolean resetPlayerHeadCache() {
		GlobalCaches.cachedPlayerHeadsMap = new HashMap<>();
		return true;
	}
}
