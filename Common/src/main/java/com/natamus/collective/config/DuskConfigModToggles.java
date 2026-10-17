package com.natamus.collective.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.util.CollectiveReference;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DuskConfigModToggles {
	public static final String modRegistry = "mod";
	public static final String disabledSerilumModsKey = "disabledSerilumMods";

	private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	private static final Set<String> knownModIds = ConcurrentHashMap.newKeySet();
	private static volatile boolean collectiveConfigLoaded = false;

	public static void initCollectiveConfig() {
		CollectiveConfigHandler.initConfig();
		collectiveConfigLoaded = true;
	}

	public static void addKnownMod(String modid) {
		if (!DuskConfigMod.isAlwaysEnabled(modid)) {
			knownModIds.add(modid);
		}
	}

	public static List<String> getKnownModIds() {
		List<String> modids = new ArrayList<>(knownModIds);
		Collections.sort(modids);
		return modids;
	}

	public static List<String> getListableModIds() {
		List<String> modids = new ArrayList<>();
		for (String modid : getKnownModIds()) {
			if (!DuskConfigMod.hasRegisteredContent(modid)) {
				modids.add(modid);
			}
		}
		return modids;
	}

	public static boolean hasConfigScreen(String modid) {
		if (DuskConfigMod.configMods.containsKey(modid)) {
			return true;
		}
		return knownModIds.contains(modid) && !DuskConfigMod.hasRegisteredContent(modid);
	}

	public static void addListOnlyMods() {
		for (String modid : getKnownModIds()) {
			if (hasConfigScreen(modid)) {
				DuskConfigMod.addListOnlyMod(modid);
			}
		}
	}

	public static boolean isDisabledInList(Path gameConfigDirectory, String modid) {
		return ConfigFunctions.isInIdList(getDisabledMods(gameConfigDirectory), modid);
	}

	public static void disableEverywhere(Path gameConfigDirectory, String modid) {
		setListed(gameConfigDirectory, modid, true);
		setModEnableValue(gameConfigDirectory, modid, false);
	}

	public static void onConfigChanged(String modid) {
		Path gameConfigDirectory = DataFunctions.getGameConfigDirectoryPath();
		if (modid.equals(CollectiveReference.MOD_ID)) {
			for (DuskConfigMod configMod : DuskConfigMod.configMods.values()) {
				if (configMod.enableEntry != null) {
					setModEnableValue(gameConfigDirectory, configMod.modid, !isDisabledInList(gameConfigDirectory, configMod.modid));
				}
			}
			return;
		}

		DuskConfigMod configMod = DuskConfigMod.configMods.get(modid);
		if (configMod == null || configMod.enableEntry == null) {
			return;
		}
		setListed(gameConfigDirectory, modid, !Boolean.TRUE.equals(configMod.enableEntry.getValue()));
	}

	private static String getDisabledMods(Path gameConfigDirectory) {
		if (collectiveConfigLoaded) {
			return CollectiveConfigHandler.disabledSerilumMods;
		}

		DuskConfigMigration.moveToSerilumConfigDirectory(gameConfigDirectory, CollectiveReference.MOD_ID);
		String disabledMods = ConfigFunctions.getConfigValues(getSerilumConfigDirectory(gameConfigDirectory), CollectiveReference.MOD_ID).get(disabledSerilumModsKey);
		if (disabledMods == null) {
			return "";
		}
		return disabledMods;
	}

	private static void setListed(Path gameConfigDirectory, String modid, boolean disabled) {
		if (isDisabledInList(gameConfigDirectory, modid) == disabled) {
			return;
		}

		List<String> disabledIds = new ArrayList<>();
		for (String rawId : getDisabledMods(gameConfigDirectory).split(",")) {
			String id = rawId.strip();
			if (!id.isEmpty() && !id.equalsIgnoreCase(modid)) {
				disabledIds.add(id);
			}
		}
		if (disabled) {
			disabledIds.add(modid.toLowerCase(Locale.ROOT));
		}
		String disabledMods = String.join(",", disabledIds);

		if (collectiveConfigLoaded) {
			DuskConfigMod collectiveConfig = DuskConfigMod.configMods.get(CollectiveReference.MOD_ID);
			CollectiveConfigHandler.disabledSerilumMods = disabledMods;
			DuskConfigFile.writeValue(collectiveConfig, collectiveConfig.getEntry(disabledSerilumModsKey));
			return;
		}

		patchConfigFile(gameConfigDirectory, CollectiveReference.MOD_ID, disabledSerilumModsKey, disabledMods);
	}

	private static void setModEnableValue(Path gameConfigDirectory, String modid, boolean enabled) {
		if (collectiveConfigLoaded) {
			DuskConfigMod configMod = DuskConfigMod.configMods.get(modid);
			if (configMod != null) {
				if (configMod.enableEntry != null && !Boolean.valueOf(enabled).equals(configMod.enableEntry.getValue())) {
					configMod.enableEntry.setValue(enabled);
					DuskConfigFile.writeValue(configMod, configMod.enableEntry);
				}
				return;
			}
		}

		String enableKey = ConfigFunctions.getModEnableKey(getSerilumConfigDirectory(gameConfigDirectory), modid);
		if (enableKey != null) {
			patchConfigFile(gameConfigDirectory, modid, enableKey, enabled);
		}
	}

	private static void patchConfigFile(Path gameConfigDirectory, String modid, String key, Object value) {
		Path configPath = getConfigFilePath(gameConfigDirectory, modid);
		JsonObject json = new JsonObject();
		if (Files.isRegularFile(configPath)) {
			try {
				json = ConfigFunctions.readConfigJson(configPath);
			}
			catch (Exception ignored) {
				return;
			}
		}

		JsonElement jsonValue = gson.toJsonTree(value);
		JsonElement existing = json.get(key);
		if (existing != null && ConfigFunctions.isNestedEntry(existing)) {
			existing.getAsJsonObject().add("value", jsonValue);
		}
		else {
			JsonObject nested = new JsonObject();
			nested.add("value", jsonValue);
			json.add(key, nested);
		}

		try {
			StringWriter stringWriter = new StringWriter();
			JsonWriter jsonWriter = new JsonWriter(stringWriter);
			jsonWriter.setIndent("\t");
			gson.toJson(json, jsonWriter);

			Files.createDirectories(configPath.getParent());
			Files.writeString(configPath, stringWriter.toString());
		}
		catch (IOException ignored) { }
	}

	private static Path getConfigFilePath(Path gameConfigDirectory, String modid) {
		Path json5Path = getSerilumConfigDirectory(gameConfigDirectory).resolve(modid + ".json5");
		if (Files.isRegularFile(json5Path)) {
			return json5Path;
		}
		return getSerilumConfigDirectory(gameConfigDirectory).resolve(modid + ".json");
	}

	private static Path getSerilumConfigDirectory(Path gameConfigDirectory) {
		return gameConfigDirectory.resolve(ConfigFunctions.serilumConfigFolderName);
	}
}
