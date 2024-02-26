package com.natamus.collective.functions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.natamus.collective.data.Constants;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

public class GearFunctions {
	public static void setPlayerGearFromGearString(Player player, String gearString) {
		setPlayerGearFromGearString(player, gearString, false);
	}
	public static void setPlayerGearFromGearString(Player player, String gearString, boolean parseActiveEffects) {
		Registry<MobEffect> mobEffectRegistry = null;
		if (parseActiveEffects) {
			mobEffectRegistry = player.level.registryAccess().registryOrThrow(Registry.MOB_EFFECT_REGISTRY);

			player.removeAllEffects();
		}

		boolean emptiedInventory = false;
		for (String line : getGearStringSplit(gearString)) {
			String slotString = getSlotStringFromLine(line);
			if (slotString.equalsIgnoreCase("effects") && parseActiveEffects) {
				if (mobEffectRegistry == null) {
					continue;
				}

				List<MobEffectInstance> mobEffectInstanceList = getEffectsFromLine(mobEffectRegistry, line);
				for (MobEffectInstance mobEffectInstance : mobEffectInstanceList) {
					player.addEffect(mobEffectInstance);
				}

				continue;
			}

			ItemStack itemStack = getItemStackFromGearStringLine(line);

			if (itemStack == null) {
				continue;
			}

			if (itemStack.isEmpty()) {
				continue;
			}

			if (!emptiedInventory) {
				player.getInventory().clearContent();
				emptiedInventory = true;
			}

			if (NumberFunctions.isNumeric(slotString)) {
				player.getInventory().setItem(Integer.parseInt(slotString), itemStack);
				continue;
			}

			EquipmentSlot equipmentSlot = getEquipmentSlotFromSlotString(slotString);
			if (equipmentSlot != null) {
				player.setItemSlot(equipmentSlot, itemStack);
			}
		}
	}

	public static void setInventoryFromGearString(Inventory inventory, String gearString) {
		for (String line : getGearStringSplit(gearString)) {
			ItemStack itemStack = getItemStackFromGearStringLine(line);

			if (itemStack == null) {
				continue;
			}

			if (itemStack.isEmpty()) {
				continue;
			}

			String slotString = getSlotStringFromLine(line);
			if (NumberFunctions.isNumeric(slotString)) {
				inventory.setItem(Integer.parseInt(slotString), itemStack);
				continue;
			}

			EquipmentSlot equipmentSlot = getEquipmentSlotFromSlotString(slotString);
			if (equipmentSlot.equals(EquipmentSlot.HEAD)) {
				inventory.armor.set(3, itemStack);
			}
			if (equipmentSlot.equals(EquipmentSlot.CHEST)) {
				inventory.armor.set(2, itemStack);
			}
			if (equipmentSlot.equals(EquipmentSlot.LEGS)) {
				inventory.armor.set(1, itemStack);
			}
			if (equipmentSlot.equals(EquipmentSlot.FEET)) {
				inventory.armor.set(0, itemStack);
			}
			if (equipmentSlot.equals(EquipmentSlot.OFFHAND)) {
				inventory.offhand.set(0, itemStack);
			}
		}
	}

	public static String getGearStringFromPlayer(Player player) {
		return getGearStringFromPlayer(player, false);
	}
	public static String getGearStringFromPlayer(Player player, boolean parseActiveEffects) {
		StringBuilder gearStringBuilder = new StringBuilder();

		for (EquipmentSlot equipmentSlot : Constants.equipmentSlots) {
			if (!gearStringBuilder.isEmpty()) {
				gearStringBuilder.append("\n");
			}

			String equipmentSlotString = equipmentSlot.getName();

			ItemStack equipmentSlotStack = player.getItemBySlot(equipmentSlot);
			if (!equipmentSlotStack.isEmpty()) {
				String nbtString = getFormattedNBTStringFromItemStack(equipmentSlotStack);
				gearStringBuilder.append("'").append(equipmentSlotString).append("'").append(" : ").append("'").append(nbtString).append("',");
			}
			else {
				gearStringBuilder.append("'").append(equipmentSlotString).append("'").append(" : ").append("'',");
			}
		}

		Inventory playerInventory = player.getInventory();
		for (int i=0; i < 36; i++) {
			ItemStack slotStack = playerInventory.getItem(i);
			if (!slotStack.isEmpty()) {
				String nbtString = getFormattedNBTStringFromItemStack(slotStack);
				gearStringBuilder.append("\n").append(i).append(" : ").append("'").append(nbtString).append("',");
			}
			else {
				gearStringBuilder.append("\n").append(i).append(" : '',");
			}
		}

		if (parseActiveEffects) {
			StringBuilder effectsStringBuilder = new StringBuilder();

			Map<MobEffect, MobEffectInstance> activeEffectsMap = player.getActiveEffectsMap();
			if (activeEffectsMap.size() > 0) {
				Registry<MobEffect> mobEffectRegistry = player.level.registryAccess().registryOrThrow(Registry.MOB_EFFECT_REGISTRY);

				List<String> effectResourceLocationList = new ArrayList<String>();
				HashMap<String, String> effectLineData = new HashMap<String, String>();

				for (MobEffect mobEffect : activeEffectsMap.keySet()) {
					MobEffectInstance mobEffectInstance = activeEffectsMap.get(mobEffect);

					ResourceLocation resourceLocation = mobEffectRegistry.getKey(mobEffect);
					if (resourceLocation == null) {
						continue;
					}

					String rlString = resourceLocation.toString();
					int level = mobEffectInstance.getAmplifier()+1;
					int duration = mobEffectInstance.getDuration();

					effectResourceLocationList.add(rlString);
					effectLineData.put(rlString, rlString + ";lvl:" + level + ";duration:" + duration);
				}

				Collections.sort(effectResourceLocationList);

				for (String rlString : effectResourceLocationList) {
					String lineData = effectLineData.get(rlString);

					if (!effectsStringBuilder.toString().equals("")) {
						effectsStringBuilder.append("|");
					}

					effectsStringBuilder.append(lineData);
				}
			}

			gearStringBuilder.append("\n'effects' : '").append(effectsStringBuilder).append("',");
		}

		return gearStringBuilder.toString();
	}

	public static String getPlayerGearStringFromHashMap(HashMap<String, ItemStack> gearStringHashMap) {
		StringBuilder gearStringBuilder = new StringBuilder();

		for (EquipmentSlot equipmentSlot : Constants.equipmentSlots) {
			if (!gearStringBuilder.isEmpty()) {
				gearStringBuilder.append("\n");
			}

			String equipmentSlotString = equipmentSlot.getName();

			String nbtString = "";
			if (gearStringHashMap.containsKey(equipmentSlotString)) {
				nbtString = getFormattedNBTStringFromItemStack(gearStringHashMap.get(equipmentSlotString));
			}

			gearStringBuilder.append("'").append(equipmentSlotString).append("'").append(" : ").append("'").append(nbtString).append("',");
		}

		List<ItemStack> emptyInventoryList = NonNullList.withSize(36, ItemStack.EMPTY);
		for (int i = 0; i < emptyInventoryList.size(); i++) {
			String nbtString = "";
			if (gearStringHashMap.containsKey(i + "")) {
				nbtString = getFormattedNBTStringFromItemStack(gearStringHashMap.get("" + i));
			}

			gearStringBuilder.append("\n").append(i).append(" : '").append(nbtString).append("',");
		}

		return gearStringBuilder.toString();
	}

	public static HashMap<String, ItemStack> getHashMapFromGearString(String gearString) {
		HashMap<String, ItemStack> gearHashMap = new HashMap<String, ItemStack>();

		for (String line : getGearStringSplit(gearString)) {
			String slotString = getSlotStringFromLine(line);
			ItemStack itemStack = getItemStackFromGearStringLine(line);

			if (itemStack == null) {
				itemStack = ItemStack.EMPTY.copy();
			}

			gearHashMap.put(slotString, itemStack);
		}

		return gearHashMap;
	}
	public static List<ItemStack> getItemStackListFromGearString(String gearString) {
		List<ItemStack> gearList = new ArrayList<ItemStack>();

		for (String line : getGearStringSplit(gearString)) {
			ItemStack itemStack = getItemStackFromGearStringLine(line);

			if (itemStack == null) {
				continue;
			}

			gearList.add(itemStack);
		}

		return gearList;
	}

	public static List<MobEffectInstance> getEffectsFromGearString(Player player, String gearString) {
		return getEffectsFromGearString(player.level, gearString);
	}
	public static List<MobEffectInstance> getEffectsFromGearString(Level level, String gearString) {
		return getEffectsFromGearString(level.registryAccess().registryOrThrow(Registry.MOB_EFFECT_REGISTRY), gearString);
	}
	public static List<MobEffectInstance> getEffectsFromGearString(Registry<MobEffect> mobEffectRegistry, String gearString) {
		for (String line : getGearStringSplit(gearString)) {
			String slotString = getSlotStringFromLine(line);
			if (slotString.equalsIgnoreCase("effects")) {
				return getEffectsFromLine(mobEffectRegistry, line);
			}
		}

		return new ArrayList<MobEffectInstance>();
	}

	public static boolean areTwoGearStringsEqual(String gearStringOne, String gearStringTwo) {
		List<ItemStack> itemStackListOne = getItemStackListFromGearString(gearStringOne);
		List<ItemStack> itemStackListTwo = getItemStackListFromGearString(gearStringTwo);

		for (ItemStack itemStackOne : itemStackListOne) {
			Item itemOne = itemStackOne.getItem();

			boolean foundMatch = false;
			for (ItemStack itemStackTwo : itemStackListTwo) {
				Item itemTwo = itemStackTwo.getItem();
				if (itemOne.equals(itemTwo)) {
					if (itemStackOne.getCount() == itemStackTwo.getCount()) {
						foundMatch = true;
						break;
					}
				}
			}

			if (!foundMatch) {
				return false;
			}
		}

		return true;
	}

	public static String sortGearString(String gearString) {
		StringBuilder sortedGearString = new StringBuilder();

		HashMap<String, String> gearHashMap = new HashMap<String, String>();
		for (String line : getGearStringSplit(gearString)) {
			String slotString = getSlotStringFromLine(line);
			String data = getDataFromLine(line);

			gearHashMap.put(slotString, data);
		}

		for (EquipmentSlot equipmentSlot : Constants.equipmentSlots) {
			String equipmentSlotName = equipmentSlot.getName();
			if (gearHashMap.containsKey(equipmentSlotName)) {
				sortedGearString.append("'").append(equipmentSlotName).append("' : '").append(gearHashMap.get(equipmentSlotName)).append("',").append(System.lineSeparator());
			}
		}

		for (String line : getGearStringSplit(gearString)) {
			String slotString = getSlotStringFromLine(line);
			String data = getDataFromLine(line);

			if (NumberFunctions.isNumeric(slotString)) {
				sortedGearString.append("'").append(slotString).append("' : '").append(data).append("',").append(System.lineSeparator());
			}
		}

		if (gearHashMap.containsKey("effects")) {
			sortedGearString.append("'effects' : '").append(gearHashMap.get("effects")).append("',").append(System.lineSeparator());
		}

		return sortedGearString.toString().strip();
	}

	private static ItemStack getItemStackFromGearStringLine(String line) {
		ItemStack itemStack = ItemStack.EMPTY;

		String[] stringArray = getStringArrayFromLine(line);
		if (stringArray.length != 2) {
			return null;
		}

		String data = getDataFromStringArray(stringArray);

		try {
			CompoundTag compoundTag = parseFormattedTag(data);
			itemStack = ItemStack.of(compoundTag);
		} catch (CommandSyntaxException ex0) {
			try {
				data = extendedFormatDataString(data);

				CompoundTag compoundTag = parseFormattedTag(data);
				itemStack = ItemStack.of(compoundTag);
			} catch (CommandSyntaxException ex1) {
				return null;
			}
		}

		return itemStack.copy();
	}

	private static List<MobEffectInstance> getEffectsFromLine(Player player, String line) {
		return getEffectsFromLine(player.level, line);
	}
	private static List<MobEffectInstance> getEffectsFromLine(Level level, String line) {
		return getEffectsFromLine(level.registryAccess().registryOrThrow(Registry.MOB_EFFECT_REGISTRY), line);
	}
	private static List<MobEffectInstance> getEffectsFromLine(Registry<MobEffect> mobEffectRegistry, String line) {
		List<MobEffectInstance> mobEffectInstanceList = new ArrayList<MobEffectInstance>();

		String effectData = getDataFromLine(line);
		for (String effectRaw : effectData.split("\\|")) {
			String[] effectSpl = effectRaw.split(";");
			if (effectSpl.length != 3) {
				continue;
			}

			String rlString = effectSpl[0];
			String lvlRaw = effectSpl[1];
			String durationRaw = effectSpl[2];

			if (!lvlRaw.contains(":") || !durationRaw.contains(":")) {
				continue;
			}

			String lvlString = lvlRaw.split(":")[1].strip();
			String durationString = durationRaw.split(":")[1].strip();

			if (!NumberFunctions.isNumeric(lvlString) || !NumberFunctions.isNumeric(durationString)) {
				continue;
			}

			int lvl = Integer.parseInt(lvlString);
			if (lvl == 0) {
				continue;
			}

			int duration = Integer.parseInt(durationString);

			MobEffect mobEffect = mobEffectRegistry.get(new ResourceLocation(rlString));
			if (mobEffect == null) {
				continue;
			}

			MobEffectInstance mobEffectInstance = new MobEffectInstance(mobEffect, duration, lvl-1);

			mobEffectInstanceList.add(mobEffectInstance);
		}

		return mobEffectInstanceList;
	}

	private static String[] getGearStringSplit(String gearString) {
		return gearString.split("',[\\r\\n]+");
	}

	private static String getSlotStringFromLine(String line) {
		return getStringArrayFromLine(line)[0].replace("'", "").strip();
	}

	private static String getDataFromStringArray(String[] stringArray) {
		if (stringArray.length < 2) {
			return "";
		}

		String rawData = stringArray[1];

		if (rawData.startsWith("'")) {
			rawData = rawData.substring(1);
		}

		if (rawData.endsWith("'")) {
			rawData = rawData.substring(0, rawData.length() - 1);
		}

		return rawData.replaceAll("\r", "");
	}

	private static String[] getStringArrayFromLine(String line) {
		line = line.trim();
		if (line.endsWith(",")) {
			line = line.substring(0, line.length() - 1);
		}
		if (!line.endsWith("'")) {
			line = line + "'";
		}

		return line.split(" : ", 2);
	}

	private static String getDataFromLine(String line) {
		return getDataFromStringArray(getStringArrayFromLine(line));
	}

	private static EquipmentSlot getEquipmentSlotFromSlotString(String slotString) {
		for (EquipmentSlot equipmentSlot : Constants.equipmentSlots) {
			if (equipmentSlot.getName().equalsIgnoreCase(slotString.strip())) {
				return equipmentSlot;
			}
		}

		return null;
	}

	private static String getFormattedNBTStringFromItemStack(ItemStack itemStack) {
		String nbtString = ItemFunctions.getNBTStringFromItemStack(itemStack);
		nbtString = nbtString.replaceAll("[\\r\\n]", "|n").replace("'", "‵");

		return nbtString;
	}
	private static CompoundTag parseFormattedTag(String data) throws CommandSyntaxException {
		data = data.replace("|n", "\n").replace("‵", "'");

		return TagParser.parseTag(data);
	}

	private static String extendedFormatDataString(String rawData) {
		return rawData.replace("\",\"", "|||,|||").replace(" \"", " '").replace("\" ", "' ").replace("|||,|||", "\",\"");
	}
}
