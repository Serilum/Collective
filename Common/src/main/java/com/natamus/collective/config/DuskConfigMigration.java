package com.natamus.collective.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.functions.NumberFunctions;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class DuskConfigMigration {
	private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	public static void moveToSerilumConfigDirectory(String modid) {
		moveToSerilumConfigDirectory(DataFunctions.getGameConfigDirectoryPath(), modid);
	}

	public static void moveToSerilumConfigDirectory(Path gameConfigDirectory, String modid) {
		Path serilumConfigDirectory = gameConfigDirectory.resolve(ConfigFunctions.serilumConfigFolderName);

		moveModConfigPaths(gameConfigDirectory, serilumConfigDirectory, modid);
		moveModConfigPaths(gameConfigDirectory.resolve(ConfigFunctions.tveConfigFolderName), serilumConfigDirectory.resolve(ConfigFunctions.tveConfigFolderName), modid);
	}

	public static void moveBundleConfigFiles(Path gameConfigDirectory) {
		File[] oldConfigFiles = gameConfigDirectory.toFile().listFiles();
		if (oldConfigFiles == null) {
			return;
		}

		Path serilumConfigDirectory = gameConfigDirectory.resolve(ConfigFunctions.serilumConfigFolderName);
		for (File oldConfigFile : oldConfigFiles) {
			if (ConfigFunctions.isBundleConfigFile(oldConfigFile.getName())) {
				moveConfigPath(oldConfigFile.toPath(), serilumConfigDirectory.resolve(oldConfigFile.getName()));
			}
		}
	}

	public static boolean importOldConfigFiles(DuskConfigMod configMod) {
		boolean imported = false;
		try {
			Path json5Path = configMod.path.resolveSibling(configMod.modid + ".json5");
			if (Files.exists(json5Path)) {
				Files.move(json5Path, configMod.path, StandardCopyOption.REPLACE_EXISTING);
				imported = true;
			}

			Path tomlPath = configMod.path.resolveSibling(configMod.modid + "-common.toml");
			if (Files.exists(tomlPath)) {
				convertTomlToJson(tomlPath, configMod.path);
				imported = true;
			}
		}
		catch (IOException ignored) { }

		return imported;
	}

	public static boolean addMergedOldSettings(DuskConfigMod configMod, JsonObject json) {
		boolean added = false;
		for (DuskConfigEntry entry : configMod.entries) {
			String name = entry.getName();
			if (json.has(name)) {
				continue;
			}

			String mergedValue = null;
			if (entry.isRgb()) {
				mergedValue = getOldRgbValue(json, name);
			}
			else if (entry.options.containsAll(List.of("Left", "Center", "Right"))) {
				mergedValue = getOldPositionValue(json, name);
			}

			if (mergedValue == null) {
				continue;
			}

			json.addProperty(name, mergedValue);
			added = true;
		}
		return added;
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

	private static void convertTomlToJson(Path tomlPath, Path jsonPath) throws IOException {
		JsonObject json = new JsonObject();

		for (String line : Files.readString(tomlPath, StandardCharsets.UTF_8).split("\n")) {
			line = line.trim();
			if (line.startsWith("[") || line.startsWith("#")) {
				continue;
			}

			String[] linespl = line.split(" = ", 2);
			if (linespl.length != 2) {
				continue;
			}

			String key = linespl[0];
			String rawValue = linespl[1].strip();

			if (rawValue.equals("true") || rawValue.equals("false")) {
				json.addProperty(key, Boolean.parseBoolean(rawValue));
			}
			else if (NumberFunctions.isNumeric(rawValue)) {
				json.addProperty(key, new BigDecimal(rawValue));
			}
			else if (rawValue.length() >= 2 && rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
				json.addProperty(key, rawValue.substring(1, rawValue.length() - 1).replace("\\\"", "\""));
			}
			else {
				json.addProperty(key, rawValue);
			}
		}

		Files.writeString(jsonPath, gson.toJson(json));
		Files.delete(tomlPath);
	}

	private static String getOldRgbValue(JsonObject json, String name) {
		String red = getOldValue(json, name + "_R");
		String green = getOldValue(json, name + "_G");
		String blue = getOldValue(json, name + "_B");
		if (red == null || green == null || blue == null) {
			return null;
		}
		return red + "," + green + "," + blue;
	}

	private static String getOldPositionValue(JsonObject json, String name) {
		if (getOldValue(json, name + "IsLeft") == null) {
			return null;
		}

		for (String position : List.of("Left", "Center")) {
			if ("true".equalsIgnoreCase(getOldValue(json, name + "Is" + position))) {
				return position;
			}
		}
		return "Right";
	}

	private static String getOldValue(JsonObject json, String key) {
		JsonElement jsonEntry = json.get(key);
		if (jsonEntry == null) {
			return null;
		}
		return ConfigFunctions.getValueString(ConfigFunctions.getEntryValue(jsonEntry));
	}
}
