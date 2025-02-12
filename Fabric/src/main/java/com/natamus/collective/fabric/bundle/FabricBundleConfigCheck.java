package com.natamus.collective.fabric.bundle;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FabricBundleConfigCheck {
	private static final HashMap<String, Boolean> bundleConfigCache = new HashMap<>();
	private static final Pattern quotePattern = Pattern.compile("\"([^\"]*)\"");

	public static boolean isBundleModEnabled(String modId) {
		if (bundleConfigCache.containsKey(modId)) {
			return bundleConfigCache.get(modId);
		}

		File configDir = new File(FabricLoader.getInstance().getGameDir().toString() + File.separator + "config");

		File[] listOfFiles = configDir.listFiles();
		if (listOfFiles == null) {
			return true;
		}

		for (File configFile : listOfFiles) {
			String fileName = configFile.getName();
			if (!fileName.endsWith("json5")) {
				continue;
			}

			if (fileName.startsWith("serilum") && fileName.contains("bundle")) {
				try {
					String configContent = Files.readString(configFile.toPath());
					for (String line : configContent.split("\n")) {
						String strippedLine = line.replace(" ", "").toLowerCase().strip();

						Matcher matcher = quotePattern.matcher(strippedLine);
						while (matcher.find()) {
							String configKey = matcher.group(1);
							if (configKey.startsWith("enable")) {
								String configModId = configKey.replaceFirst("enable", "").strip();
								bundleConfigCache.put(configModId, strippedLine.endsWith("true,"));
							}
						}
					}
				}
				catch (IOException ignored) { }
			}
		}

		if (bundleConfigCache.containsKey(modId)) {
			return bundleConfigCache.get(modId);
		}

		return true;
	}
}