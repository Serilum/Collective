package com.natamus.collective.features;

import com.mojang.authlib.GameProfile;
import com.natamus.collective.data.FeatureFlags;
import com.natamus.collective.functions.HeadFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class PlayerHeadCacheFeature {
	public static HashMap<String, ItemStack> cachedPlayerHeadsMap = new HashMap<String, ItemStack>();
	public static HashMap<String, UUID> cachedPlayerNamesMap = new HashMap<String, UUID>();
	public static HashMap<UUID, String> cachedPlayerUUIDsMap = new HashMap<UUID, String>();
	public static HashMap<UUID, GameProfile> cachedGameProfileMap = new HashMap<UUID, GameProfile>();

	public static ItemStack cachePlayer(Player player) {
		return cachePlayer(player.getName().getString(), player.getGameProfile());
	}
	public static ItemStack cachePlayer(ServerLevel serverLevel, String playerName) {
		if (cachedPlayerHeadsMap.containsKey(playerName)) {
			return cachedPlayerHeadsMap.get(playerName).copy();
		}

		ItemStack headStack = HeadFunctions.getNewPlayerHead(serverLevel, playerName, 1);
		if (headStack == null) {
			return null;
		}

		cachedPlayerHeadsMap.put(playerName, headStack);

		return headStack.copy();
	}
	public static ItemStack cachePlayer(String playerName, GameProfile gameProfile) {
		if (cachedPlayerHeadsMap.containsKey(playerName)) {
			return cachedPlayerHeadsMap.get(playerName).copy();
		}

		ItemStack headStack = HeadFunctions.getNewPlayerHead(gameProfile, 1);
		if (headStack == null) {
			return null;
		}

		cachedPlayerHeadsMap.put(gameProfile.getName(), headStack);

		return headStack.copy();
	}

	public static ItemStack getPlayerHeadStackFromCache(Player player) {
		return cachePlayer(player);
	}
	public static ItemStack getPlayerHeadStackFromCache(ServerLevel serverLevel, String playerName) {
		if (cachedPlayerHeadsMap.containsKey(playerName)) {
			return cachedPlayerHeadsMap.get(playerName).copy();
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
		cachedPlayerHeadsMap = new HashMap<String, ItemStack>();
		return true;
	}
}
