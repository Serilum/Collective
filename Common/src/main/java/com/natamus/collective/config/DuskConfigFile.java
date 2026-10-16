package com.natamus.collective.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.natamus.collective.data.Constants;
import com.natamus.collective.functions.ColourFunctions;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.functions.NumberFunctions;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DuskConfigFile {
	private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	public static void load(DuskConfigMod configMod) {
		if (configMod.isDisabled) {
			loadEnableValue(configMod);
			return;
		}

		boolean rewrite = importOldConfigFiles(configMod);

		if (!Files.exists(configMod.path)) {
			resetValues(configMod);
			write(configMod);
			return;
		}

		JsonObject json;
		try {
			json = ConfigFunctions.readConfigJson(configMod.path);
		}
		catch (Exception ex) {
			resetValues(configMod);

			Path unreadablePath = configMod.path.resolveSibling(configMod.modid + ".json.unreadable");
			try {
				Files.move(configMod.path, unreadablePath, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException moveException) {
				Constants.LOG.warn("[" + configMod.name + "] Unable to read " + configMod.path.getFileName() + " (" + ex.getMessage() + ") and unable to keep a copy of it, so it was left untouched. The default settings are used until it is fixed.");
				return;
			}

			Constants.LOG.warn("[" + configMod.name + "] Unable to read " + configMod.path.getFileName() + " (" + ex.getMessage() + "). It was kept as " + unreadablePath.getFileName() + " and a default config was written.");
			write(configMod);
			return;
		}

		int settingCount = json.size();
		if (json.has("//")) {
			settingCount -= 1;
			if (!getFileHeader(configMod).equals(ConfigFunctions.getValueString(json.get("//")))) {
				rewrite = true;
			}
		}
		else {
			rewrite = true;
		}

		int found = 0;
		for (DuskConfigEntry entry : configMod.entries) {
			JsonElement jsonEntry = json.get(entry.getName());
			if (jsonEntry == null) {
				entry.resetValue();
				rewrite = true;
				continue;
			}

			found += 1;
			if (!readValue(entry, ConfigFunctions.getEntryValue(jsonEntry))) {
				entry.resetValue();
				rewrite = true;
			}
			else if (!ConfigFunctions.isNestedEntry(jsonEntry)) {
				rewrite = true;
			}
		}

		if (rewrite || found != settingCount) {
			write(configMod);
		}
	}

	public static void write(DuskConfigMod configMod) {
		if (configMod.isDisabled) {
			writeValue(configMod, configMod.enableEntry);
			return;
		}

		JsonObject json = new JsonObject();
		json.addProperty("//", getFileHeader(configMod));

		for (DuskConfigEntry entry : configMod.entries) {
			json.add(entry.getName(), getJsonEntry(entry));
		}

		writeJson(configMod, json);
	}

	private static void loadEnableValue(DuskConfigMod configMod) {
		try {
			JsonElement jsonEntry = ConfigFunctions.readConfigJson(configMod.path).get(configMod.enableEntry.getName());
			if (jsonEntry != null) {
				readValue(configMod.enableEntry, ConfigFunctions.getEntryValue(jsonEntry));
			}
		}
		catch (Exception ignored) { }
	}

	public static void writeValue(DuskConfigMod configMod, DuskConfigEntry entry) {
		JsonObject json;
		try {
			json = ConfigFunctions.readConfigJson(configMod.path);
		}
		catch (Exception ex) {
			Constants.LOG.warn("[" + configMod.name + "] Unable to read " + configMod.path.getFileName() + " (" + ex.getMessage() + "), so " + entry.getName() + " was not saved.");
			return;
		}

		json.add(entry.getName(), getJsonEntry(entry));
		writeJson(configMod, json);
	}

	public static boolean reload(DuskConfigMod configMod) {
		if (Files.exists(configMod.path)) {
			try {
				ConfigFunctions.readConfigJson(configMod.path);
			}
			catch (Exception ignored) {
				return false;
			}
		}

		load(configMod);
		return true;
	}

	private static JsonObject getJsonEntry(DuskConfigEntry entry) {
		JsonObject jsonEntry = new JsonObject();

		String comment = entry.getComment();
		if (!comment.isEmpty()) {
			jsonEntry.addProperty("//", comment);
		}

		jsonEntry.add("value", gson.toJsonTree(entry.getValue()));
		return jsonEntry;
	}

	private static void writeJson(DuskConfigMod configMod, JsonObject json) {
		try {
			StringWriter stringWriter = new StringWriter();
			JsonWriter jsonWriter = new JsonWriter(stringWriter);
			jsonWriter.setIndent("\t");
			gson.toJson(json, jsonWriter);

			Files.createDirectories(configMod.path.getParent());
			Files.writeString(configMod.path, stringWriter.toString());
		}
		catch (IOException ex) {
			Constants.LOG.warn("[" + configMod.name + "] Unable to save " + configMod.path.getFileName() + " (" + ex.getMessage() + ").");
		}
	}

	private static String getFileHeader(DuskConfigMod configMod) {
		return "These settings are easier to change in-game! Mod List -> " + configMod.name + " -> Config";
	}

	private static void resetValues(DuskConfigMod configMod) {
		for (DuskConfigEntry entry : configMod.entries) {
			entry.resetValue();
		}
	}

	private static boolean readValue(DuskConfigEntry entry, JsonElement value) {
		if (entry.hasOptions()) {
			String option = entry.findOption(ConfigFunctions.getValueString(value));
			if (option == null) {
				return false;
			}

			entry.setOption(option);
			return true;
		}

		Object readValue;
		try {
			readValue = gson.fromJson(value, entry.getGenericType());
		}
		catch (Exception ignored) {
			return false;
		}

		if (readValue == null) {
			return false;
		}
		if (readValue instanceof Number number && !entry.isValidNumber(number)) {
			return false;
		}

		if (entry.isRgb()) {
			int colour = ColourFunctions.getRgbColour(String.valueOf(readValue));
			if (colour == -1) {
				return false;
			}
			readValue = ColourFunctions.getRgbString(colour);
		}

		entry.setValue(readValue);
		if (entry.hasRegistry()) {
			entry.setIdList(entry.getIdList());
		}
		return true;
	}

	private static boolean importOldConfigFiles(DuskConfigMod configMod) {
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
}
