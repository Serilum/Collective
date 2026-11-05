package com.natamus.collective.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.natamus.collective.data.GlobalCaches;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.services.Services;
import com.natamus.collective.util.CollectiveReference;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DuskConfigModToggles {
	public static final String modRegistry = "mod";
	public static final String disabledSerilumModsKey = "disabledSerilumMods";

	public static final String collectiveListSwitch = "collective_list";
	public static final String modConfigSwitch = "mod_config";
	public static final String bundleConfigSwitch = "bundle_config";

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

	public static boolean isDisabledSerilumMod(String modid) {
		if (!knownModIds.contains(modid)) {
			return false;
		}
		return isDisabledInList(DataFunctions.getGameConfigDirectoryPath(), modid);
	}

	public static synchronized boolean syncModSwitches(Path gameConfigDirectory, String modid, boolean isBundled) {
		if (DuskConfigMod.isAlwaysEnabled(modid)) {
			return true;
		}

		Map<String, Boolean> modSwitches = getModSwitches(gameConfigDirectory, modid, isBundled);
		Map<String, Boolean> lastSyncedSwitches = DuskConfigToggleCache.getLastSyncedSwitches(gameConfigDirectory, modid);

		boolean enabled = getSyncedValue(modSwitches, lastSyncedSwitches);
		setModSwitches(gameConfigDirectory, modid, modSwitches, enabled);
		return enabled;
	}

	public static void disableEverywhere(Path gameConfigDirectory, String modid) {
		setEnabledEverywhere(gameConfigDirectory, modid, Services.MODLOADER.isJarJard(modid), false);
	}

	public static void onConfigChanged(String modid) {
		Path gameConfigDirectory = DataFunctions.getGameConfigDirectoryPath();
		if (modid.equals(CollectiveReference.MOD_ID)) {
			for (String switchableModId : getSwitchableModIds()) {
				boolean enabled = !isDisabledInList(gameConfigDirectory, switchableModId);
				setEnabledEverywhere(gameConfigDirectory, switchableModId, Services.MODLOADER.isJarJard(switchableModId), enabled);
			}
			return;
		}

		DuskConfigMod configMod = DuskConfigMod.configMods.get(modid);
		if (configMod == null) {
			return;
		}

		if (ConfigFunctions.isBundleModId(modid)) {
			for (DuskConfigEntry entry : configMod.entries) {
				String bundledModId = ConfigFunctions.getBundledModId(entry.getName());
				if (bundledModId != null) {
					setEnabledEverywhere(gameConfigDirectory, bundledModId, true, Boolean.TRUE.equals(entry.getValue()));
				}
			}
			return;
		}

		if (configMod.enableEntry != null) {
			setEnabledEverywhere(gameConfigDirectory, modid, Services.MODLOADER.isJarJard(modid), Boolean.TRUE.equals(configMod.enableEntry.getValue()));
		}
	}

	private static Set<String> getSwitchableModIds() {
		Set<String> modids = new HashSet<>(knownModIds);
		for (DuskConfigMod configMod : DuskConfigMod.configMods.values()) {
			if (configMod.enableEntry != null) {
				modids.add(configMod.modid);
			}
		}
		return modids;
	}

	private static void setEnabledEverywhere(Path gameConfigDirectory, String modid, boolean isBundled, boolean enabled) {
		Map<String, Boolean> modSwitches = getModSwitches(gameConfigDirectory, modid, isBundled);
		setModSwitches(gameConfigDirectory, modid, modSwitches, enabled);
	}

	private static Map<String, Boolean> getModSwitches(Path gameConfigDirectory, String modid, boolean isBundled) {
		Map<String, Boolean> modSwitches = new LinkedHashMap<>();
		modSwitches.put(collectiveListSwitch, !isDisabledInList(gameConfigDirectory, modid));

		Boolean enabledInModConfig = getModConfigValue(gameConfigDirectory, modid);
		if (enabledInModConfig != null) {
			modSwitches.put(modConfigSwitch, enabledInModConfig);
		}

		if (isBundled) {
			Boolean enabledInBundleConfig = getBundleConfigValue(gameConfigDirectory, modid);
			if (enabledInBundleConfig != null) {
				modSwitches.put(bundleConfigSwitch, enabledInBundleConfig);
			}
		}

		return modSwitches;
	}

	private static Boolean getModConfigValue(Path gameConfigDirectory, String modid) {
		if (collectiveConfigLoaded) {
			DuskConfigMod configMod = DuskConfigMod.configMods.get(modid);
			if (configMod != null && configMod.enableEntry != null) {
				return Boolean.TRUE.equals(configMod.enableEntry.getValue());
			}
		}

		return ConfigFunctions.getModEnableValue(getSerilumConfigDirectory(gameConfigDirectory), modid);
	}

	private static boolean getSyncedValue(Map<String, Boolean> modSwitches, Map<String, Boolean> lastSyncedSwitches) {
		List<Boolean> changedValues = new ArrayList<>();
		List<Boolean> unchangedValues = new ArrayList<>();

		for (Map.Entry<String, Boolean> modSwitch : modSwitches.entrySet()) {
			Boolean lastSyncedValue = lastSyncedSwitches.get(modSwitch.getKey());
			if (lastSyncedValue == null) {
				continue;
			}

			if (lastSyncedValue.equals(modSwitch.getValue())) {
				unchangedValues.add(modSwitch.getValue());
			}
			else {
				changedValues.add(modSwitch.getValue());
			}
		}

		if (!changedValues.isEmpty()) {
			return areAllEnabled(changedValues);
		}
		if (!unchangedValues.isEmpty()) {
			return areAllEnabled(unchangedValues);
		}
		return areAllEnabled(modSwitches.values());
	}

	private static boolean areAllEnabled(Collection<Boolean> switchValues) {
		return !switchValues.contains(false);
	}

	private static void setModSwitches(Path gameConfigDirectory, String modid, Map<String, Boolean> modSwitches, boolean enabled) {
		for (Map.Entry<String, Boolean> modSwitch : modSwitches.entrySet()) {
			if (modSwitch.getValue().equals(enabled)) {
				continue;
			}

			switch (modSwitch.getKey()) {
				case collectiveListSwitch -> setListed(gameConfigDirectory, modid, !enabled);
				case modConfigSwitch -> setModEnableValue(gameConfigDirectory, modid, enabled);
				case bundleConfigSwitch -> setBundleConfigValue(gameConfigDirectory, modid, enabled);
			}
		}

		DuskConfigToggleCache.setLastSyncedSwitches(gameConfigDirectory, modid, modSwitches.keySet(), enabled);
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

	private static Boolean getBundleConfigValue(Path gameConfigDirectory, String modid) {
		if (!GlobalCaches.bundleConfigCache.containsKey(modid)) {
			GlobalCaches.bundleConfigCache.putAll(ConfigFunctions.getBundleModToggles(gameConfigDirectory));
		}
		return GlobalCaches.bundleConfigCache.get(modid);
	}

	private static void setBundleConfigValue(Path gameConfigDirectory, String modid, boolean enabled) {
		for (File bundleConfigFile : ConfigFunctions.getBundleConfigFiles(gameConfigDirectory)) {
			String enableKey = getBundleEnableKey(bundleConfigFile, modid);
			if (enableKey != null) {
				setBundleEnableValue(gameConfigDirectory, getBundleModId(bundleConfigFile), enableKey, enabled);
			}
		}

		GlobalCaches.bundleConfigCache.put(modid, enabled);
	}

	private static void setBundleEnableValue(Path gameConfigDirectory, String bundleModId, String enableKey, boolean enabled) {
		if (collectiveConfigLoaded) {
			DuskConfigMod bundleConfig = DuskConfigMod.configMods.get(bundleModId);
			if (bundleConfig != null) {
				DuskConfigEntry enableEntry = bundleConfig.getEntry(enableKey);
				if (enableEntry != null) {
					enableEntry.setValue(enabled);
					DuskConfigFile.writeValue(bundleConfig, enableEntry);
					return;
				}
			}
		}

		patchConfigFile(gameConfigDirectory, bundleModId, enableKey, enabled);
	}

	private static String getBundleEnableKey(File bundleConfigFile, String modid) {
		try {
			for (String key : ConfigFunctions.readConfigJson(bundleConfigFile.toPath()).keySet()) {
				if (modid.equals(ConfigFunctions.getBundledModId(key))) {
					return key;
				}
			}
		}
		catch (Exception ignored) { }
		return null;
	}

	private static String getBundleModId(File bundleConfigFile) {
		return bundleConfigFile.getName().replace(".json5", "").replace(".json", "");
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
