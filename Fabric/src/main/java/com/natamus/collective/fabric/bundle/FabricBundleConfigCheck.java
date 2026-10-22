package com.natamus.collective.fabric.bundle;

import com.natamus.collective.data.GlobalCaches;
import com.natamus.collective.functions.ConfigFunctions;
import net.fabricmc.loader.api.FabricLoader;

public class FabricBundleConfigCheck {
	public static boolean isBundleModEnabled(String modId) {
		if (GlobalCaches.bundleConfigCache.containsKey(modId)) {
			return GlobalCaches.bundleConfigCache.get(modId);
		}

		GlobalCaches.bundleConfigCache.putAll(ConfigFunctions.getBundleModToggles(FabricLoader.getInstance().getGameDir().resolve("config")));

		if (GlobalCaches.bundleConfigCache.containsKey(modId)) {
			return GlobalCaches.bundleConfigCache.get(modId);
		}

		return true;
	}

	public static boolean isModEnabled(String modId) {
		if (!GlobalCaches.modEnabledCache.containsKey(modId)) {
			GlobalCaches.modEnabledCache.put(modId, ConfigFunctions.isModEnabled(FabricLoader.getInstance().getGameDir().resolve("config"), modId));
		}
		return GlobalCaches.modEnabledCache.get(modId);
	}
}
