package com.natamus.collective.functions;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.natamus.collective.features.PlayerHeadCacheFeature;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;
import java.util.UUID;

public class HeadFunctions {
	public static ItemStack getNewPlayerHead(ServerLevel serverLevel, String playerName, Integer amount) {
		return getNewPlayerHead(serverLevel, playerName, "", amount);
	}
	public static ItemStack getNewPlayerHead(ServerLevel serverLevel, String playerName, String noteBlockSound, Integer amount) {
		GameProfile gameProfile = getGameProfileFromPlayerName(serverLevel, playerName);
		if (gameProfile == null) {
			return null;
		}

		return getNewPlayerHead(gameProfile, noteBlockSound, amount);
	}
	public static ItemStack getNewPlayerHead(GameProfile gameProfile, Integer amount) {
		return getNewPlayerHead(gameProfile, "", amount);
	}
	public static ItemStack getNewPlayerHead(GameProfile gameProfile, String noteBlockSound, Integer amount) {
		if (gameProfile == null) {
			return null;
		}

		String playerName = gameProfile.name();
		PropertyMap propertyMap = gameProfile.properties();

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

		ItemStack playerHeadStack = getNewTexturedHead(playerName, textures, gameProfile.id(), noteBlockSound, amount);
		playerHeadStack.set(DataComponents.CUSTOM_NAME, Component.literal(playerName + "'s Head"));

		return playerHeadStack;
	}

	public static ItemStack getNewTexturedHead(String entityName, String texture, String uuidString, Integer amount) {
		return getNewTexturedHead(entityName, texture, uuidString, "", amount);
	}
	public static ItemStack getNewTexturedHead(String entityName, String texture, String uuidString, String noteBlockSound, Integer amount) {
		UUID uuid = UUIDFunctions.getUUIDFromStringLenient(uuidString);
		int[] intArray = UUIDUtil.uuidToIntArray(uuid);

		return getNewTexturedHead(entityName, texture, uuid, noteBlockSound, amount);
	}
	public static ItemStack getNewTexturedHead(String entityName, String texture, UUID uuid, Integer amount) {
		return getNewTexturedHead(entityName, texture, uuid, "", amount);
	}
	public static ItemStack getNewTexturedHead(String entityName, String texture, UUID uuid, String noteBlockSound, Integer amount) {
		ItemStack texturedHeadStack = new ItemStack(Items.PLAYER_HEAD, amount);

		GameProfile gameProfile = getTexturedHeadGameProfile(entityName, texture, uuid);

		texturedHeadStack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile));

		if (!noteBlockSound.isEmpty()) {
			texturedHeadStack.set(DataComponents.NOTE_BLOCK_SOUND, ResourceLocation.parse(noteBlockSound));
		}

		return texturedHeadStack;
	}

	public static GameProfile getTexturedHeadGameProfile(String entityName, String texture, String uuidString) {
		return getTexturedHeadGameProfile(entityName, texture, UUIDFunctions.getUUIDFromStringLenient(uuidString));
	}
	public static GameProfile getTexturedHeadGameProfile(String entityName, String texture, UUID uuid) {
		if (entityName.length() > 16) {
			entityName = entityName.substring(0, 16);
		}

		GameProfile gameProfile = new GameProfile(uuid, entityName.replace(" ", "_"));

		PropertyMap propertyMap = gameProfile.properties();
		Multimap<String, Property> mutableProperties = HashMultimap.create(propertyMap);

		mutableProperties.put("textures", new Property("textures", texture));

		return new GameProfile(uuid, entityName.replace(" ", "_"), new PropertyMap(mutableProperties));
	}
	public static ResolvableProfile getTexturedHeadResolvableProfile(String entityName, String texture, String uuidString) {
		return getTexturedHeadResolvableProfile(entityName, texture, UUIDFunctions.getUUIDFromStringLenient(uuidString));
	}
	public static ResolvableProfile getTexturedHeadResolvableProfile(String entityName, String texture, UUID uuid) {
		return ResolvableProfile.createResolved(getTexturedHeadGameProfile(entityName, texture, uuid));
	}

	public static GameProfile getGameProfileFromPlayerName(ServerLevel serverLevel, String playerName) {
		MinecraftSessionService minecraftSessionService = serverLevel.getServer().services().sessionService();

		MinecraftServer minecraftServer = serverLevel.getServer();
		UserNameToIdResolver userNameToIdResolver = minecraftServer.services().nameToIdCache();

		UUID headUUID;
		if (PlayerHeadCacheFeature.cachedPlayerNamesMap.containsKey(playerName.toLowerCase())) {
			headUUID = PlayerHeadCacheFeature.cachedPlayerNamesMap.get(playerName.toLowerCase());
			playerName = PlayerHeadCacheFeature.cachedPlayerUUIDsMap.get(headUUID);
		}
		else {
			Optional<NameAndId> optionalNameAndId = userNameToIdResolver.get(playerName);
			if (optionalNameAndId.isEmpty()) {
				return null;
			}

			headUUID = optionalNameAndId.get().id();

			Optional<GameProfile> optionalGameProfile = minecraftServer.services().profileResolver().fetchByName(playerName);
			if (optionalGameProfile.isEmpty()) {
				optionalGameProfile = minecraftServer.services().profileResolver().fetchById(headUUID);
				if (optionalGameProfile.isEmpty()) {
					return null;
				}
			}

			GameProfile playerNameGameProfile = optionalGameProfile.get();

			PlayerHeadCacheFeature.cachedPlayerNamesMap.put(playerNameGameProfile.name().toLowerCase(), headUUID);
			PlayerHeadCacheFeature.cachedPlayerUUIDsMap.put(headUUID, playerNameGameProfile.name());
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
	
	public static boolean hasStandardHead(String mobname) {
        return mobname.equals("creeper") || mobname.equals("zombie") || mobname.equals("skeleton");
    }
}
