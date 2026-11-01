package com.natamus.collective.config.screen;

import com.natamus.collective.config.DuskConfigEntry;
import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.functions.DataFunctions;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuskConfigRegistryCache {
	public static void saveWorldRegistryIds() {
		for (String registryName : getPickerRegistryNames()) {
			if (!DuskConfigRegistryIds.needsWorld(registryName) || DuskConfigRegistryIds.isBuiltInRegistry(registryName)) {
				continue;
			}

			List<String> ids = DuskConfigRegistryIds.getLiveIds(registryName);
			if (!ids.isEmpty()) {
				writeIds(registryName, ids);
			}
		}
	}

	public static List<String> loadIds(String registryName) {
		List<String> ids = new ArrayList<>();
		Path cacheFile = getCacheFile(registryName);
		if (!Files.isRegularFile(cacheFile)) {
			return ids;
		}

		try {
			for (String line : Files.readString(cacheFile, StandardCharsets.UTF_8).split("\n")) {
				if (!line.isBlank()) {
					ids.add(line.strip());
				}
			}
		}
		catch (IOException ignored) { }
		return ids;
	}

	private static Set<String> getPickerRegistryNames() {
		Set<String> registryNames = new HashSet<>();
		for (DuskConfigMod configMod : DuskConfigMod.configMods.values()) {
			for (DuskConfigEntry entry : configMod.entries) {
				if (!entry.registry.isEmpty()) {
					registryNames.add(entry.registry);
				}
			}
		}
		return registryNames;
	}

	private static void writeIds(String registryName, List<String> ids) {
		Path cacheFile = getCacheFile(registryName);
		File cacheFolder = cacheFile.getParent().toFile();
		if (!cacheFolder.isDirectory() && !cacheFolder.mkdirs()) {
			return;
		}

		try {
			PrintWriter writer = new PrintWriter(cacheFile.toFile(), StandardCharsets.UTF_8);
			writer.print(String.join("\n", ids));
			writer.close();
		}
		catch (IOException ignored) { }
	}

	private static Path getCacheFile(String registryName) {
		String fileName = registryName.replace(":", "_").replace("/", "_") + ".txt";
		return DataFunctions.getGameDirectoryPath().resolve("data").resolve("serilum").resolve("cache").resolve("config").resolve(fileName);
	}
}
