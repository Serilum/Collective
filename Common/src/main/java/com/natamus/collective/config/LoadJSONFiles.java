package com.natamus.collective.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.natamus.collective.data.Constants;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.functions.JsonFunctions;
import com.natamus.collective.globalcallbacks.JSONCallback;
import com.natamus.collective.util.CollectiveReference;

import java.io.File;
import java.nio.file.Files;

public class LoadJSONFiles {
	public static void startListening() {
		JSONCallback.ALL_JSON_FILES_AVAILABLE.register(LoadJSONFiles::loadGlobalLists);
	}

	public static void loadGlobalLists(String folder) {
		GlobalVariables.areaNames = JsonFunctions.getStringListFromJsonFile(folder, "area_names.json", "area_names");
		GlobalVariables.femaleNames = JsonFunctions.getStringListFromJsonFile(folder, "entity_names.json", "female_names");
		GlobalVariables.maleNames = JsonFunctions.getStringListFromJsonFile(folder, "entity_names.json", "male_names");
		GlobalVariables.lingerMessages = JsonFunctions.getStringListFromJsonFile(folder, "linger_messages.json", "linger_messages");
	}

	public static void addCustomFiles() {
		addJsonList("entity_names.json", "female_names", "Female Names", "The female names given to named entities, one per line.");
		addJsonList("entity_names.json", "male_names", "Male Names", "The male names given to named entities, one per line.");
		addJsonList("area_names.json", "area_names", "Area Names", "The names given to areas and villages, one per line.");
		addJsonList("linger_messages.json", "linger_messages", "Linger Messages", "The messages shown while waiting in bed, one per line.");
	}

	private static void addJsonList(String fileName, String key, String name, String description) {
		DuskConfigCustomFile.addText(CollectiveReference.MOD_ID, fileName, name, description, () -> readJsonList(fileName, key), text -> writeJsonList(fileName, key, text));
	}

	private static String getJsonFolder() {
		return DataFunctions.getConfigDirectory() + File.separator + CollectiveReference.MOD_ID;
	}

	private static String readJsonList(String fileName, String key) {
		return String.join("\n", JsonFunctions.getStringListFromJsonFile(getJsonFolder(), fileName, key));
	}

	private static void writeJsonList(String fileName, String key, String text) {
		JsonArray lines = new JsonArray();
		for (String line : text.split("\\R")) {
			if (!line.isBlank()) {
				lines.add(line.strip());
			}
		}

		if (lines.isEmpty()) {
			Constants.LOG.warn("Not saving an empty list to {}, since mods need at least one entry.", fileName);
			return;
		}

		File file = GenerateJSONFiles.getJsonFile(fileName);
		JsonObject jsonObject;
		try {
			jsonObject = JsonParser.parseString(Files.readString(file.toPath())).getAsJsonObject();
		}
		catch (Exception ex) {
			Constants.LOG.warn("Unable to read the JSON file: {}", fileName);
			return;
		}

		jsonObject.add(key, lines);
		GenerateJSONFiles.writeJsonFile(file, jsonObject);

		loadGlobalLists(getJsonFolder());
	}
}
