package com.natamus.collective.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.natamus.collective.functions.ConfigFunctions;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class DuskConfigToggleCache {
	private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	private static final Map<String, Map<String, Boolean>> lastSyncedSwitches = new ConcurrentHashMap<>();
	private static volatile boolean cacheFileRead = false;

	public static Map<String, Boolean> getLastSyncedSwitches(Path gameConfigDirectory, String modid) {
		readCacheFile(gameConfigDirectory);
		return lastSyncedSwitches.getOrDefault(modid, new HashMap<>());
	}

	public static void setLastSyncedSwitches(Path gameConfigDirectory, String modid, Set<String> switchNames, boolean enabled) {
		Map<String, Boolean> syncedSwitches = new TreeMap<>();
		for (String switchName : switchNames) {
			syncedSwitches.put(switchName, enabled);
		}

		if (syncedSwitches.equals(lastSyncedSwitches.get(modid))) {
			return;
		}

		lastSyncedSwitches.put(modid, syncedSwitches);
		writeCacheFile(gameConfigDirectory);
	}

	private static void readCacheFile(Path gameConfigDirectory) {
		if (cacheFileRead) {
			return;
		}

		Path cachePath = getCachePath(gameConfigDirectory);
		if (Files.isRegularFile(cachePath)) {
			try {
				for (Map.Entry<String, JsonElement> modEntry : ConfigFunctions.readConfigJson(cachePath).entrySet()) {
					lastSyncedSwitches.putIfAbsent(modEntry.getKey(), getSwitchValues(modEntry.getValue().getAsJsonObject()));
				}
			}
			catch (Exception ignored) { }
		}

		cacheFileRead = true;
	}

	private static Map<String, Boolean> getSwitchValues(JsonObject switchesJson) {
		Map<String, Boolean> switchValues = new TreeMap<>();
		for (Map.Entry<String, JsonElement> switchEntry : switchesJson.entrySet()) {
			switchValues.put(switchEntry.getKey(), switchEntry.getValue().getAsBoolean());
		}
		return switchValues;
	}

	private static void writeCacheFile(Path gameConfigDirectory) {
		JsonObject json = new JsonObject();
		for (Map.Entry<String, Map<String, Boolean>> modEntry : new TreeMap<>(lastSyncedSwitches).entrySet()) {
			json.add(modEntry.getKey(), gson.toJsonTree(modEntry.getValue()));
		}

		try {
			StringWriter stringWriter = new StringWriter();
			JsonWriter jsonWriter = new JsonWriter(stringWriter);
			jsonWriter.setIndent("\t");
			gson.toJson(json, jsonWriter);

			Path cachePath = getCachePath(gameConfigDirectory);
			Files.createDirectories(cachePath.getParent());
			Files.writeString(cachePath, stringWriter.toString());
		}
		catch (IOException ignored) { }
	}

	private static Path getCachePath(Path gameConfigDirectory) {
		return gameConfigDirectory.toAbsolutePath().getParent().resolve("data").resolve("serilum").resolve("cache").resolve("config").resolve("mod_toggles.json");
	}
}
