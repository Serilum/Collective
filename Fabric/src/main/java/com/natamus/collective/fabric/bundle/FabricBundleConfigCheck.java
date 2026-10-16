package com.natamus.collective.fabric.bundle;

import com.natamus.collective.functions.ConfigFunctions;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FabricBundleConfigCheck {
	private static final Map<String, Boolean> bundleConfigCache = new ConcurrentHashMap<>();
	private static final Map<String, Boolean> modEnabledCache = new ConcurrentHashMap<>();

	public static boolean isBundleModEnabled(String modId) {
		if (bundleConfigCache.containsKey(modId)) {
			return bundleConfigCache.get(modId);
		}

		bundleConfigCache.putAll(ConfigFunctions.getBundleModToggles(FabricLoader.getInstance().getGameDir().resolve("config")));

		if (bundleConfigCache.containsKey(modId)) {
			return bundleConfigCache.get(modId);
		}

		return true;
	}

	public static boolean isModEnabled(String modId) {
		if (!modEnabledCache.containsKey(modId)) {
			modEnabledCache.put(modId, ConfigFunctions.isModEnabled(FabricLoader.getInstance().getGameDir().resolve("config"), modId));
		}
		return modEnabledCache.get(modId);
	}
}
