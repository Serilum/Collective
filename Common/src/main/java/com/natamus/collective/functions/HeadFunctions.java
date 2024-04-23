package com.natamus.collective.functions;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Base64;
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

		String encodedTextures = "";
		for (Property textureProperty : propertyMap.get("textures")) {
			if (textureProperty.name().equals("textures")) {
				encodedTextures = textureProperty.value();
				break;
			}
		}

		String decodedTextures = new String(Base64.getDecoder().decode(encodedTextures));

		StringBuilder decodedNoTimestampTextures = new StringBuilder();
		for (String line : decodedTextures.split("\n")) {
			if (!decodedNoTimestampTextures.toString().equals("")) {
				decodedNoTimestampTextures.append("\n");
			}

			if (line.contains("\"timestamp\" : ")) {
				decodedNoTimestampTextures.append("  \"timestamp\" : 0,"); // Allows stacking of heads
				continue;
			}

			decodedNoTimestampTextures.append(line);
		}

		String textures = Base64.getEncoder().encodeToString(decodedNoTimestampTextures.toString().getBytes());
		if (textures.isEmpty()) {
			return null;
		}

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
		GameProfileRepository gameProfileRepository = minecraftServer.getProfileRepository();

		UUID headUUID;
		if (PlayerHeadCacheFeature.cachedPlayerNamesMap.containsKey(playerName.toLowerCase())) {
			headUUID = PlayerHeadCacheFeature.cachedPlayerNamesMap.get(playerName.toLowerCase());
			playerName = PlayerHeadCacheFeature.cachedPlayerUUIDsMap.get(headUUID);
		}
		else {
			gameProfileRepository.findProfilesByNames(new String[]{playerName}, new ProfileLookupCallback() {
				@Override
				public void onProfileLookupSucceeded(GameProfile gameProfile) {
					PlayerHeadCacheFeature.cachedPlayerNamesMap.put(gameProfile.getName().toLowerCase(), gameProfile.getId());
					PlayerHeadCacheFeature.cachedPlayerUUIDsMap.put(gameProfile.getId(), gameProfile.getName());
				}

				@Override
				public void onProfileLookupFailed(String profileName, Exception exception) { }
			});

			return null;
		}

		GameProfile gameProfile;
		if (PlayerHeadCacheFeature.cachedGameProfileMap.containsKey(headUUID)) {
			gameProfile = PlayerHeadCacheFeature.cachedGameProfileMap.get(headUUID);
		}
		else {
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
