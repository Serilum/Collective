package com.natamus.collective.functions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.natamus.collective.config.DuskConfigMod;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigFunctions {
	public static final String serilumConfigFolderName = "serilum";
	private static final String tveConfigFolderName = "TVE";

	public static String getConfigValue(String modid, String key) {
		return getConfigValues(modid).get(key);
	}

	public static HashMap<String, String> getConfigValues(String modid) {
		moveToSerilumConfigDirectory(modid);
		return getConfigValues(DataFunctions.getConfigDirectoryPath(), modid);
	}

	public static List<String> getRawConfigValues(String modid) {
		return getRawConfigValues(modid, false);
	}

	public static List<String> getRawConfigValues(String modid, boolean tve) {
		moveToSerilumConfigDirectory(modid);

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
		moveToSerilumConfigDirectory(modid);
		return getConfigValues(getConfigDirectoryPath(tve), modid);
	}

	public static boolean isModEnabled(String modid) {
		return isModEnabled(DataFunctions.getGameConfigDirectoryPath(), modid);
	}

	public static boolean isModEnabled(Path gameConfigDirectory, String modid) {
		moveToSerilumConfigDirectory(gameConfigDirectory, modid);
		JsonObject json = getConfigJson(gameConfigDirectory.resolve(serilumConfigFolderName), modid);

		String enableKey = getModEnableKey(json);
		if (enableKey == null) {
			return true;
		}

		String enableValue = getValueString(getEntryValue(json.get(enableKey)));
		return !enableValue.equalsIgnoreCase("false");
	}

	public static String getModEnableKey(String modid) {
		return getModEnableKey(getConfigJson(DataFunctions.getConfigDirectoryPath(), modid));
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

	public static HashMap<String, Boolean> getBundleModToggles(Path gameConfigDirectory) {
		HashMap<String, Boolean> toggles = new HashMap<>();

		Path configDirectory = gameConfigDirectory.resolve(serilumConfigFolderName);
		File[] oldConfigFiles = gameConfigDirectory.toFile().listFiles();
		if (oldConfigFiles != null) {
			for (File oldConfigFile : oldConfigFiles) {
				if (isBundleConfigFile(oldConfigFile.getName())) {
					moveConfigPath(oldConfigFile.toPath(), configDirectory.resolve(oldConfigFile.getName()));
				}
			}
		}

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

	private static HashMap<String, String> getConfigValues(Path configDirectory, String modid) {
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

	public static void moveToSerilumConfigDirectory(String modid) {
		moveToSerilumConfigDirectory(DataFunctions.getGameConfigDirectoryPath(), modid);
	}

	public static void moveToSerilumConfigDirectory(Path gameConfigDirectory, String modid) {
		Path serilumConfigDirectory = gameConfigDirectory.resolve(serilumConfigFolderName);

		moveModConfigPaths(gameConfigDirectory, serilumConfigDirectory, modid);
		moveModConfigPaths(gameConfigDirectory.resolve(tveConfigFolderName), serilumConfigDirectory.resolve(tveConfigFolderName), modid);
	}

	private static void moveModConfigPaths(Path oldDirectory, Path newDirectory, String modid) {
		for (String name : List.of(modid + ".json", modid + ".json5", modid + "-common.toml", modid + ".json.unreadable", modid)) {
			moveConfigPath(oldDirectory.resolve(name), newDirectory.resolve(name));
		}
	}

	private static void moveConfigPath(Path oldPath, Path newPath) {
		if (!Files.exists(oldPath)) {
			return;
		}

		try {
			if (Files.isDirectory(oldPath)) {
				Files.createDirectories(newPath);

				File[] files = oldPath.toFile().listFiles();
				if (files != null) {
					for (File file : files) {
						moveConfigPath(file.toPath(), newPath.resolve(file.getName()));
					}
				}

				Files.delete(oldPath);
				return;
			}

			Files.createDirectories(newPath.getParent());
			Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ignored) { }
	}

	private static boolean isBundleConfigFile(String fileName) {
		if (!fileName.startsWith("serilum") || !fileName.contains("bundle")) {
			return false;
		}
		return fileName.endsWith(".json") || fileName.endsWith(".json5");
	}
}
