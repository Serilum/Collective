package com.natamus.collective.functions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.natamus.collective.config.DuskConfigMigration;
import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.objects.IdAmount;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigFunctions {
	public static final String serilumConfigFolderName = "serilum";
	public static final String tveConfigFolderName = "TVE";

	public static String getConfigValue(String modid, String key) {
		return getConfigValues(modid).get(key);
	}

	public static HashMap<String, String> getConfigValues(String modid) {
		DuskConfigMigration.moveToSerilumConfigDirectory(modid);
		return getConfigValues(DataFunctions.getConfigDirectoryPath(), modid);
	}

	public static List<String> getRawConfigValues(String modid) {
		return getRawConfigValues(modid, false);
	}

	public static List<String> getRawConfigValues(String modid, boolean tve) {
		DuskConfigMigration.moveToSerilumConfigDirectory(modid);

		List<String> values = new ArrayList<>();
		for (Map.Entry<String, JsonElement> entry : getConfigJson(getConfigDirectoryPath(tve), modid).entrySet()) {
			if (entry.getKey().equals("//")) {
				continue;
			}

			JsonElement value = getEntryValue(entry.getValue());

			String rawValue = getValueString(value);
			if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
				rawValue = "\"" + rawValue + "\"";
			}

			values.add("\"" + entry.getKey() + "\": " + rawValue);
		}
		return values;
	}

	public static HashMap<String, String> getDictValues(String modid) {
		return getDictValues(modid, false);
	}

	public static HashMap<String, String> getDictValues(String modid, boolean tve) {
		DuskConfigMigration.moveToSerilumConfigDirectory(modid);
		return getConfigValues(getConfigDirectoryPath(tve), modid);
	}

	public static boolean isModEnabled(String modid) {
		return isModEnabled(DataFunctions.getGameConfigDirectoryPath(), modid);
	}

	public static boolean isModEnabled(Path gameConfigDirectory, String modid) {
		DuskConfigMigration.moveToSerilumConfigDirectory(gameConfigDirectory, modid);
		JsonObject json = getConfigJson(gameConfigDirectory.resolve(serilumConfigFolderName), modid);

		boolean enabledInModConfig = true;
		String enableKey = getModEnableKey(json);
		if (enableKey != null) {
			enabledInModConfig = !getValueString(getEntryValue(json.get(enableKey))).equalsIgnoreCase("false");
		}

		// Both switches are kept equal when changed in-game; a hand edit that leaves them apart turns the mod off in both.
		boolean enabledInCollectiveList = !DuskConfigModToggles.isDisabledInList(gameConfigDirectory, modid);
		if (enabledInModConfig != enabledInCollectiveList) {
			DuskConfigModToggles.disableEverywhere(gameConfigDirectory, modid);
		}
		return enabledInModConfig && enabledInCollectiveList;
	}

	public static String getModEnableKey(String modid) {
		return getModEnableKey(DataFunctions.getConfigDirectoryPath(), modid);
	}

	public static String getModEnableKey(Path configDirectory, String modid) {
		return getModEnableKey(getConfigJson(configDirectory, modid));
	}

	public static boolean isInIdList(String idList, String id) {
		String idPath = id.substring(id.indexOf(':') + 1);

		for (String rawEntry : idList.split(",")) {
			String entry = rawEntry.strip().toLowerCase(Locale.ROOT);
			if (entry.isEmpty()) {
				continue;
			}

			String comparedId = idPath;
			if (entry.contains(":")) {
				comparedId = id;
			}

			if (comparedId.matches(Pattern.quote(entry).replace("*", "\\E.*\\Q"))) {
				return true;
			}
		}
		return false;
	}

	public static List<IdAmount> getIdAmounts(String idList) {
		List<IdAmount> idAmounts = new ArrayList<>();
		for (String rawEntry : idList.split(",")) {
			IdAmount idAmount = new IdAmount(rawEntry);
			if (!idAmount.id.isEmpty()) {
				idAmounts.add(idAmount);
			}
		}
		return idAmounts;
	}

	public static HashMap<String, Boolean> getBundleModToggles(Path gameConfigDirectory) {
		HashMap<String, Boolean> toggles = new HashMap<>();

		DuskConfigMigration.moveBundleConfigFiles(gameConfigDirectory);

		Path configDirectory = gameConfigDirectory.resolve(serilumConfigFolderName);
		File[] configFiles = configDirectory.toFile().listFiles();
		if (configFiles == null) {
			return toggles;
		}

		for (File configFile : configFiles) {
			String fileName = configFile.getName();
			if (!isBundleConfigFile(fileName)) {
				continue;
			}
			if (fileName.endsWith(".json") && new File(configFile.getPath() + "5").isFile()) {
				continue;
			}

			try {
				for (Map.Entry<String, JsonElement> entry : readConfigJson(configFile.toPath()).entrySet()) {
					String key = entry.getKey().replace(" ", "").toLowerCase(Locale.ROOT);
					if (key.startsWith("enable")) {
						toggles.put(key.replaceFirst("enable", ""), getValueString(getEntryValue(entry.getValue())).equals("true"));
					}
				}
			}
			catch (Exception ignored) { }
		}

		return toggles;
	}

	public static JsonObject readConfigJson(Path configPath) throws IOException {
		String content;
		try {
			content = Files.readString(configPath);
		}
		catch (MalformedInputException ignored) {
			content = Files.readString(configPath, StandardCharsets.ISO_8859_1);
		}
		String contentWithoutTrailingCommas = content.replaceAll(",(\\s*[}\\]])", "$1");
		return JsonParser.parseString(contentWithoutTrailingCommas).getAsJsonObject();
	}

	public static boolean isNestedEntry(JsonElement entry) {
		return entry.isJsonObject() && entry.getAsJsonObject().has("value");
	}

	public static JsonElement getEntryValue(JsonElement entry) {
		if (isNestedEntry(entry)) {
			return entry.getAsJsonObject().get("value");
		}
		return entry;
	}

	public static String getValueString(JsonElement value) {
		if (value.isJsonNull()) {
			return "";
		}
		if (value.isJsonPrimitive()) {
			return value.getAsString();
		}
		return value.toString();
	}

	public static HashMap<String, String> getConfigValues(Path configDirectory, String modid) {
		HashMap<String, String> values = new HashMap<>();
		for (Map.Entry<String, JsonElement> entry : getConfigJson(configDirectory, modid).entrySet()) {
			if (entry.getKey().equals("//")) {
				continue;
			}

			values.put(entry.getKey(), getValueString(getEntryValue(entry.getValue())));
		}
		return values;
	}

	private static JsonObject getConfigJson(Path configDirectory, String modid) {
		Path configPath = configDirectory.resolve(modid + ".json5");
		if (!Files.isRegularFile(configPath)) {
			configPath = configDirectory.resolve(modid + ".json");
		}

		try {
			return readConfigJson(configPath);
		}
		catch (Exception ignored) { }
		return new JsonObject();
	}

	private static String getModEnableKey(JsonObject json) {
		for (String key : json.keySet()) {
			if (key.startsWith(DuskConfigMod.enableKeyPrefix)) {
				return key;
			}
		}
		return null;
	}

	private static Path getConfigDirectoryPath(boolean tve) {
		if (tve) {
			return DataFunctions.getConfigDirectoryPath().resolve(tveConfigFolderName);
		}
		return DataFunctions.getConfigDirectoryPath();
	}

	public static boolean isBundleConfigFile(String fileName) {
		if (!fileName.startsWith("serilum") || !fileName.contains("bundle")) {
			return false;
		}
		return fileName.endsWith(".json") || fileName.endsWith(".json5");
	}
}
