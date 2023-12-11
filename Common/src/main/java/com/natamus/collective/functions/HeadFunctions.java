package com.natamus.collective.functions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.natamus.collective.data.GlobalVariables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class HeadFunctions {
	public static ItemStack getPlayerHead(String playername, Integer amount) {
		String nameIdJsonString = DataFunctions.readStringFromURL(GlobalVariables.playerdataurl + playername.toLowerCase());
		if (nameIdJsonString.equals("")) {
			return null;
		}

		try {
			Map<String, String> nameIdJson = new Gson().fromJson(nameIdJsonString, new TypeToken<HashMap<String, String>>() {}.getType());

			String headName = nameIdJson.get("name");
			String headUUID = nameIdJson.get("id");

			String profileJsonString = DataFunctions.readStringFromURL(GlobalVariables.skindataurl + headUUID);
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
	
	public static boolean hasStandardHead(String mobname) {
        return mobname.equals("creeper") || mobname.equals("zombie") || mobname.equals("skeleton");
    }
}
