package com.natamus.collective.functions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.features.PlayerHeadCacheFeature;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

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
			if (textureProperty.getName().equals("textures")) {
				encodedTextures = textureProperty.getValue();
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

		UUID headUUID;
		if (PlayerHeadCacheFeature.cachedPlayerNamesMap.containsKey(playerName.toLowerCase())) {
			headUUID = PlayerHeadCacheFeature.cachedPlayerNamesMap.get(playerName.toLowerCase());
			playerName = PlayerHeadCacheFeature.cachedPlayerUUIDsMap.get(headUUID);
		}
		else {
			String nameIdJsonString = DataFunctions.readStringFromURL(GlobalVariables.playerDataURL + playerName.toLowerCase());
			if (nameIdJsonString.equals("")) {
				return null;
			}

			Map<String, String> nameIdJson = new Gson().fromJson(nameIdJsonString, new TypeToken<HashMap<String, String>>() {}.getType());

			String headName = nameIdJson.get("name");
			String headUUIDRaw = nameIdJson.get("id");

			headUUID = UUIDFunctions.getUUIDFromStringLenient(headUUIDRaw);

			PlayerHeadCacheFeature.cachedPlayerNamesMap.put(headName.toLowerCase(), headUUID);
			PlayerHeadCacheFeature.cachedPlayerUUIDsMap.put(headUUID, headName);
		}

		GameProfile gameProfile;
		if (PlayerHeadCacheFeature.cachedGameProfileMap.containsKey(headUUID)) {
			gameProfile = PlayerHeadCacheFeature.cachedGameProfileMap.get(headUUID);
		}
		else {
			gameProfile = minecraftSessionService.fillProfileProperties(new GameProfile(headUUID, playerName), false);
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
			if (textureProperty.getName().equals("textures")) {
				textures = textureProperty.getValue();
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


	// Legacy Code for backwards compatibility
	public static ItemStack getPlayerHead(String playername, Integer amount) {
		String nameIdJsonString = DataFunctions.readStringFromURL(GlobalVariables.playerDataURL + playername.toLowerCase());
		if (nameIdJsonString.equals("")) {
			return null;
		}

		try {
			Map<String, String> nameIdJson = new Gson().fromJson(nameIdJsonString, new TypeToken<HashMap<String, String>>() {}.getType());

			String headName = nameIdJson.get("name");
			String headUUID = nameIdJson.get("id");

			String profileJsonString = DataFunctions.readStringFromURL(GlobalVariables.skinDataURL + headUUID);
			if (profileJsonString.equals("")) {
				return null;
			}

			String[] rawValue = profileJsonString.replaceAll(" ", "").split("value\":\"");

			String texturevalue = rawValue[1].split("\"")[0];
			String d = new String(Base64.getDecoder().decode((texturevalue.getBytes())));

			String texture = Base64.getEncoder().encodeToString((("{\"textures\"" + d.split("\"textures\"")[1]).getBytes()));
			String oldid = new UUID(texture.hashCode(), texture.hashCode()).toString();

			return HeadFunctions.getTexturedHead(headName + "'s Head", texture, oldid, 1);
		}
		catch (ArrayIndexOutOfBoundsException ignored) { }

		return null;
	}
	public static ItemStack getTexturedHead(String headname, String texture, String oldid, Integer amount) {
		ItemStack texturedhead = new ItemStack(Items.PLAYER_HEAD, amount);

		List<Integer> intarray = UUIDFunctions.oldIdToIntArray(oldid);

		CompoundTag skullOwner = new CompoundTag();
		skullOwner.putIntArray("Id", intarray);

		CompoundTag properties = new CompoundTag();
		ListTag textures = new ListTag();
		CompoundTag tex = new CompoundTag();
		tex.putString("Value", texture);
		textures.add(tex);

		properties.put("textures", textures);
		skullOwner.put("Properties", properties);
		texturedhead.addTagElement("SkullOwner", skullOwner);

		Component tcname = Component.literal(headname);
		texturedhead.setHoverName(tcname);
		return texturedhead;
	}
}
