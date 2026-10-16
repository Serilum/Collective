package com.natamus.collective.neoforge.bundle;

import com.natamus.collective.functions.ConfigFunctions;
import net.neoforged.fml.loading.FMLPaths;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NeoForgeBundleConfigCheck {
	private static final Map<String, Boolean> bundleConfigCache = new ConcurrentHashMap<>();
	private static final Map<String, Boolean> modEnabledCache = new ConcurrentHashMap<>();

	public static boolean isBundleModEnabled(String modId) {
		if (bundleConfigCache.containsKey(modId)) {
			return bundleConfigCache.get(modId);
		}

		bundleConfigCache.putAll(ConfigFunctions.getBundleModToggles(FMLPaths.GAMEDIR.get().resolve("config")));

		if (bundleConfigCache.containsKey(modId)) {
			return bundleConfigCache.get(modId);
		}

		return true;
	}

	public static boolean isModEnabled(String modId) {
		if (!modEnabledCache.containsKey(modId)) {
			modEnabledCache.put(modId, ConfigFunctions.isModEnabled(FMLPaths.GAMEDIR.get().resolve("config"), modId));
		}
		return modEnabledCache.get(modId);
	}
}
