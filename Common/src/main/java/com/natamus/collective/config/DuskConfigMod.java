package com.natamus.collective.config;

import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.services.Services;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DuskConfigMod {
	public static final Map<String, DuskConfigMod> configMods = new ConcurrentHashMap<>();
	public static final String enableKeyPrefix = "Enable ";

	private static volatile Set<String> contentNamespaces = Set.of();
	private static volatile int contentNamespacesRegistrySize = -1;

	public final String name;
	public final String modid;
	public final Path path;
	public final List<DuskConfigEntry> entries = new ArrayList<>();
	public final DuskConfigEntry enableEntry;
	public final boolean isDisabled;
	public final boolean isListOnly;

	public DuskConfigMod(String name, String modid, Class<?> configClass) {
		this.name = name;
		this.modid = modid;
		this.path = DataFunctions.getConfigDirectoryPath().resolve(modid + ".json");
		this.isDisabled = false;
		this.isListOnly = false;

		HashMap<String, List<String>> configMetaData = getConfigMetaData(configClass);
		for (Field field : configClass.getDeclaredFields()) {
			if (!field.isAnnotationPresent(DuskConfig.Entry.class)) {
				continue;
			}

			List<String> description = configMetaData.getOrDefault(field.getName(), new ArrayList<>());
			entries.add(new DuskConfigEntry(field, description));
		}

		if (canBeDisabled()) {
			this.enableEntry = createEnableEntry(name);
			entries.add(0, enableEntry);
		}
		else {
			this.enableEntry = null;
		}
	}

	private DuskConfigMod(String name, String modid) {
		this.name = name;
		this.modid = modid;
		this.path = DataFunctions.getConfigDirectoryPath().resolve(modid + ".json");
		this.isDisabled = true;
		this.isListOnly = false;
		this.enableEntry = createEnableEntry(name);

		enableEntry.setValue(false);
		entries.add(enableEntry);
	}

	private DuskConfigMod(String name, String modid, boolean enabled) {
		this.name = name;
		this.modid = modid;
		this.path = DataFunctions.getConfigDirectoryPath().resolve(modid + ".json");
		this.isDisabled = false;
		this.isListOnly = true;
		this.enableEntry = createEnableEntry(name);

		enableEntry.setValue(enabled);
		entries.add(enableEntry);
	}

	public static DuskConfigMod addListOnlyMod(String modid) {
		DuskConfigMod configMod = configMods.get(modid);
		if (configMod != null) {
			return configMod;
		}

		boolean enabled = !DuskConfigModToggles.isDisabledInList(DataFunctions.getGameConfigDirectoryPath(), modid);
		configMod = new DuskConfigMod(Services.MODLOADER.getModName(modid), modid, enabled);
		configMods.put(modid, configMod);
		return configMod;
	}

	public static void addDisabledMod(String modid) {
		String enableKey = ConfigFunctions.getModEnableKey(modid);
		if (enableKey == null) {
			return;
		}

		String name = enableKey.substring(enableKeyPrefix.length());
		configMods.put(modid, new DuskConfigMod(name, modid));
	}

	public DuskConfigEntry getEntry(String name) {
		for (DuskConfigEntry entry : entries) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null;
	}

	public List<DuskConfigEntry> getActiveEntries() {
		if (enableEntry == null || isDisabled || isListOnly) {
			return entries;
		}
		if (!hasRegisteredContent(modid)) {
			return entries;
		}

		List<DuskConfigEntry> activeEntries = new ArrayList<>(entries);
		activeEntries.remove(enableEntry);
		return activeEntries;
	}

	public static boolean isAlwaysEnabled(String modid) {
		if (modid.equals(CollectiveReference.MOD_ID) || modid.equals("hideexperimentalwarning")) {
			return true;
		}
		return modid.startsWith("serilum") && modid.contains("bundle");
	}

	private boolean canBeDisabled() {
		if (isAlwaysEnabled(modid)) {
			return false;
		}

		String enableFieldName = "enable" + name.replaceAll("[^A-Za-z0-9]", "");
		for (DuskConfigEntry entry : entries) {
			if (entry.getName().equalsIgnoreCase(enableFieldName)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public static boolean hasRegisteredContent(String modid) {
		int contentRegistrySize = BuiltInRegistries.BLOCK.size() + BuiltInRegistries.ITEM.size() + BuiltInRegistries.ENTITY_TYPE.size() + BuiltInRegistries.SOUND_EVENT.size();
		if (contentRegistrySize != contentNamespacesRegistrySize) {
			contentNamespaces = getContentNamespaces();
			contentNamespacesRegistrySize = contentRegistrySize;
		}
		return contentNamespaces.contains(modid);
	}

	@SuppressWarnings("deprecation")
	private static Set<String> getContentNamespaces() {
		Set<String> namespaces = new HashSet<>();
		List<Set<ResourceLocation>> contentIds = Arrays.asList(BuiltInRegistries.BLOCK.keySet(), BuiltInRegistries.ITEM.keySet(), BuiltInRegistries.ENTITY_TYPE.keySet(), BuiltInRegistries.SOUND_EVENT.keySet());
		for (Set<ResourceLocation> ids : contentIds) {
			for (ResourceLocation id : ids) {
				namespaces.add(id.getNamespace());
			}
		}
		return namespaces;
	}

	private static DuskConfigEntry createEnableEntry(String name) {
		List<String> description = Arrays.asList("Whether " + name + " is enabled. When disabled, none of its features run and its other settings are hidden. Takes effect after a restart.");
		return new DuskConfigEntry(enableKeyPrefix + name, description, true);
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, List<String>> getConfigMetaData(Class<?> configClass) {
		try {
			Object configMetaData = configClass.getDeclaredField("configMetaData").get(null);
			if (configMetaData instanceof HashMap) {
				return (HashMap<String, List<String>>) configMetaData;
			}
		}
		catch (Exception ignored) { }

		return new HashMap<>();
	}
}
