package com.natamus.collective.functions;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.natamus.collective.features.PlayerHeadCacheFeature;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.UUID;

public class HeadFunctions {
	public static ItemStack getNewPlayerHead(ServerLevel serverLevel, String playerName, Integer amount) {
		GameProfile gameProfile = getGameProfileFromPlayerName(serverLevel, playerName);
		if (gameProfile == null) {
			return null;
		}

		return getNewPlayerHead(gameProfile, amount);
	}
	public static ItemStack getNewPlayerHead(GameProfile gameProfile, Integer amount) {
		if (gameProfile == null) {
			return null;
		}

		PropertyMap propertyMap = gameProfile.getProperties();

		String texturePropertyValue = "";
		for (Property textureProperty : propertyMap.get("textures")) {
			if (textureProperty.name().equals("textures")) {
				texturePropertyValue = textureProperty.value();
				break;
			}
		}

		if (!texturePropertyValue.contains("cHJvZmlsZUlk")) {
			return null;
		}

		// Set timestamp to 0 to make heads stackable
		String textures = "ewogICJ0aW1lc3RhbXAiIDogMCwKICAicHJvZmlsZUlk" + texturePropertyValue.split("cHJvZmlsZUlk")[1];

		int[] headIntArray = UUIDUtil.uuidToIntArray(gameProfile.getId());

		return getNewTexturedHead(gameProfile.getName(), textures, headIntArray, amount);
	}

	public static ItemStack getNewTexturedHead(String playerName, String texture, String uuidString, Integer amount) {
		UUID uuid = UUIDFunctions.getUUIDFromStringLenient(uuidString);
		int[] intArray = UUIDUtil.uuidToIntArray(uuid);

		return getNewTexturedHead(playerName, texture, intArray, amount);
	}
	public static ItemStack getNewTexturedHead(String playerName, String texture, int[] idIntArray, Integer amount) {
		ItemStack texturedHeadStack = new ItemStack(Items.PLAYER_HEAD, amount);

		CompoundTag skullOwnerCompoundTag = getSkullOwnerCompoundTag(playerName, texture, idIntArray);
		texturedHeadStack.addTagElement("SkullOwner", skullOwnerCompoundTag);

		Component tcname = Component.literal(playerName + "'s Head");
		texturedHeadStack.setHoverName(tcname);
		return texturedHeadStack;
	}

	public static GameProfile getGameProfileFromPlayerName(ServerLevel serverLevel, String playerName) {
		MinecraftSessionService minecraftSessionService = serverLevel.getServer().getSessionService();

		MinecraftServer minecraftServer = serverLevel.getServer();
		GameProfileCache gameProfileCache = minecraftServer.getProfileCache();

		UUID headUUID;
		if (PlayerHeadCacheFeature.cachedPlayerNamesMap.containsKey(playerName.toLowerCase())) {
			headUUID = PlayerHeadCacheFeature.cachedPlayerNamesMap.get(playerName.toLowerCase());
			playerName = PlayerHeadCacheFeature.cachedPlayerUUIDsMap.get(headUUID);
		}
		else {
			Optional<GameProfile> playerNameGameProfileOptional = gameProfileCache.get(playerName);
			if (playerNameGameProfileOptional.isEmpty()) {
				return null;
			}

			GameProfile playerNameGameProfile = playerNameGameProfileOptional.get();
			if (playerNameGameProfile == null) {
				return null;
			}

			headUUID = playerNameGameProfile.getId();

			PlayerHeadCacheFeature.cachedPlayerNamesMap.put(playerNameGameProfile.getName().toLowerCase(), headUUID);
			PlayerHeadCacheFeature.cachedPlayerUUIDsMap.put(headUUID, playerNameGameProfile.getName());
		}

		GameProfile gameProfile;
		if (PlayerHeadCacheFeature.cachedGameProfileMap.containsKey(headUUID)) {
			gameProfile = PlayerHeadCacheFeature.cachedGameProfileMap.get(headUUID);
		} else {
			ProfileResult profileResult = minecraftSessionService.fetchProfile(headUUID, false);
			if (profileResult == null) {
				return null;
			}

			gameProfile = profileResult.profile();
			if (gameProfile == null) {
				return null;
			}

			PlayerHeadCacheFeature.cachedGameProfileMap.put(headUUID, gameProfile);
		}

		return gameProfile;
	}

	public static CompoundTag getSkullOwnerCompoundTag(ServerLevel serverLevel, String playerName) {
		GameProfile gameProfile = getGameProfileFromPlayerName(serverLevel, playerName);
		PropertyMap propertyMap = gameProfile.getProperties();

		String textures = "";
		for (Property textureProperty : propertyMap.get("textures")) {
			if (textureProperty.name().equals("textures")) {
				textures = textureProperty.value();
			}
		}

		if (textures.equals("")) {
			return null;
		}

		int[] headIntArray = UUIDUtil.uuidToIntArray(gameProfile.getId());

		return getSkullOwnerCompoundTag(playerName, textures, headIntArray);
	}
	public static CompoundTag getSkullOwnerCompoundTag(String playerName, String textures, int[] idIntArray) {
		CompoundTag skullOwnerCompoundTag = new CompoundTag();
		skullOwnerCompoundTag.putString("Name", playerName);
		skullOwnerCompoundTag.putIntArray("Id", idIntArray);

		CompoundTag propertiesCompoundTag = new CompoundTag();
		ListTag texturesListTag = new ListTag();
		CompoundTag texturesCompoundTag = new CompoundTag();
		texturesCompoundTag.putString("Value", textures);
		texturesListTag.add(texturesCompoundTag);

		propertiesCompoundTag.put("textures", texturesListTag);
		skullOwnerCompoundTag.put("Properties", propertiesCompoundTag);

		return skullOwnerCompoundTag;
	}
	
	public static boolean hasStandardHead(String mobname) {
        return mobname.equals("creeper") || mobname.equals("zombie") || mobname.equals("skeleton");
    }
}
