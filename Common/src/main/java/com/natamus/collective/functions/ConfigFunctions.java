package com.natamus.collective.functions;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.natamus.collective.config.DuskConfigEntry;
import com.natamus.collective.config.DuskConfigMigration;
import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.objects.IdAmount;
import com.natamus.collective.services.Services;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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

	public static String getConfigString(String modid, String key, String fallback) {
		if (getLoadedConfigValue(modid, key) instanceof String value) {
			return value;
		}
		return fallback;
	}
	public static String getConfigString(MinecraftServer server, String modid, String key, String fallback) {
		JsonElement worldValue = getWorldConfigValue(server, modid, key);
		if (worldValue != null && worldValue.isJsonPrimitive()) {
			return worldValue.getAsString();
		}
		return getConfigString(modid, key, fallback);
	}

	public static boolean getConfigBoolean(String modid, String key, boolean fallback) {
		if (getLoadedConfigValue(modid, key) instanceof Boolean value) {
			return value;
		}
		return fallback;
	}
	public static boolean getConfigBoolean(MinecraftServer server, String modid, String key, boolean fallback) {
		JsonElement worldValue = getWorldConfigValue(server, modid, key);
		if (worldValue != null && worldValue.isJsonPrimitive() && worldValue.getAsJsonPrimitive().isBoolean()) {
			return worldValue.getAsBoolean();
		}
		return getConfigBoolean(modid, key, fallback);
	}

	public static int getConfigInt(String modid, String key, int fallback) {
		if (getLoadedConfigValue(modid, key) instanceof Integer value) {
			return value;
		}
		return fallback;
	}
	public static int getConfigInt(MinecraftServer server, String modid, String key, int fallback) {
		JsonElement worldValue = getWorldConfigValue(server, modid, key);
		if (worldValue != null && worldValue.isJsonPrimitive() && worldValue.getAsJsonPrimitive().isNumber()) {
			return worldValue.getAsInt();
		}
		return getConfigInt(modid, key, fallback);
	}

	public static void setWorldConfigValue(MinecraftServer server, String modid, String key, String value) {
		writeWorldConfigValue(server, modid, key, new JsonPrimitive(value));
	}
	public static void setWorldConfigValue(MinecraftServer server, String modid, String key, boolean value) {
		writeWorldConfigValue(server, modid, key, new JsonPrimitive(value));
	}
	public static void setWorldConfigValue(MinecraftServer server, String modid, String key, int value) {
		writeWorldConfigValue(server, modid, key, new JsonPrimitive(value));
	}

	private static Object getLoadedConfigValue(String modid, String key) {
		DuskConfigMod configMod = getEnabledConfigMod(modid);
		if (configMod == null) {
			return null;
		}

		for (DuskConfigEntry entry : configMod.entries) {
			if (entry.getName().equals(key)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private static JsonElement getWorldConfigValue(MinecraftServer server, String modid, String key) {
		if (getEnabledConfigMod(modid) == null) {
			return null;
		}
		return getWorldConfigJson(server, modid).get(key);
	}

	private static DuskConfigMod getEnabledConfigMod(String modid) {
		DuskConfigMod configMod = DuskConfigMod.configMods.get(modid);
		if (configMod == null || configMod.isDisabled || DuskConfigModToggles.isDisabledSerilumMod(modid)) {
			return null;
		}
		return configMod;
	}

	private static JsonObject getWorldConfigJson(MinecraftServer server, String modid) {
		Path worldConfigPath = getWorldConfigPath(server, modid);
		if (!Files.isRegularFile(worldConfigPath)) {
			return new JsonObject();
		}

		try {
			return readConfigJson(worldConfigPath);
		}
		catch (Exception ignored) { }
		return new JsonObject();
	}

	private static void writeWorldConfigValue(MinecraftServer server, String modid, String key, JsonPrimitive value) {
		JsonObject worldConfigJson = getWorldConfigJson(server, modid);
		worldConfigJson.add(key, value);

		File worldConfigFile = getWorldConfigPath(server, modid).toFile();
		boolean ignored = worldConfigFile.getParentFile().mkdirs();
		writeJsonFile(worldConfigFile, worldConfigJson);
	}

	private static void writeJsonFile(File file, JsonObject json) {
		try {
			PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
			writer.println(new GsonBuilder().setPrettyPrinting().create().toJson(json));
			writer.close();
		}
		catch (IOException ignored) { }
	}

	private static Path getWorldConfigPath(MinecraftServer server, String modid) {
		return Path.of(WorldFunctions.getWorldPath(server), "data", modid, "config.json");
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
		return isModEnabled(gameConfigDirectory, modid, Services.MODLOADER.isJarJard(modid));
	}

	public static boolean isModEnabled(Path gameConfigDirectory, String modid, boolean isBundled) {
		DuskConfigMigration.moveToSerilumConfigDirectory(gameConfigDirectory, modid);
		return DuskConfigModToggles.syncModSwitches(gameConfigDirectory, modid, isBundled);
	}

	public static Boolean getModEnableValue(Path configDirectory, String modid) {
		JsonObject json = getConfigJson(configDirectory, modid);
		String enableKey = getModEnableKey(json);
		if (enableKey == null) {
			return null;
		}
		return !getValueString(getEntryValue(json.get(enableKey))).equalsIgnoreCase("false");
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

		for (File bundleConfigFile : getBundleConfigFiles(gameConfigDirectory)) {
			try {
				for (Map.Entry<String, JsonElement> entry : readConfigJson(bundleConfigFile.toPath()).entrySet()) {
					String bundledModId = getBundledModId(entry.getKey());
					if (bundledModId != null) {
						toggles.put(bundledModId, getValueString(getEntryValue(entry.getValue())).equals("true"));
					}
				}
			}
			catch (Exception ignored) { }
		}

		return toggles;
	}

	public static List<File> getBundleConfigFiles(Path gameConfigDirectory) {
		List<File> bundleConfigFiles = new ArrayList<>();

		DuskConfigMigration.moveBundleConfigFiles(gameConfigDirectory);

		File[] configFiles = gameConfigDirectory.resolve(serilumConfigFolderName).toFile().listFiles();
		if (configFiles == null) {
			return bundleConfigFiles;
		}

		for (File configFile : configFiles) {
			String fileName = configFile.getName();
			if (!isBundleConfigFile(fileName)) {
				continue;
			}
			if (fileName.endsWith(".json") && new File(configFile.getPath() + "5").isFile()) {
				continue;
			}

			bundleConfigFiles.add(configFile);
		}

		return bundleConfigFiles;
	}

	public static String getBundledModId(String bundleConfigKey) {
		String key = bundleConfigKey.replace(" ", "").toLowerCase(Locale.ROOT);
		if (!key.startsWith("enable")) {
			return null;
		}
		return key.replaceFirst("enable", "");
	}

	public static boolean isBundleModId(String modid) {
		return modid.startsWith("serilum") && modid.contains("bundle");
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
