package com.natamus.collective.forge.bundle;

import com.natamus.collective.data.GlobalCaches;
import com.natamus.collective.functions.ConfigFunctions;
import net.minecraftforge.fml.loading.FMLPaths;

public class ForgeBundleConfigCheck {
	public static boolean isBundleModEnabled(String modId) {
		if (GlobalCaches.bundleConfigCache.containsKey(modId)) {
			return GlobalCaches.bundleConfigCache.get(modId);
		}

		GlobalCaches.bundleConfigCache.putAll(ConfigFunctions.getBundleModToggles(FMLPaths.GAMEDIR.get().resolve("config")));

		if (GlobalCaches.bundleConfigCache.containsKey(modId)) {
			return GlobalCaches.bundleConfigCache.get(modId);
		}

		return true;
	}

	public static boolean isModEnabled(String modId) {
		if (!GlobalCaches.modEnabledCache.containsKey(modId)) {
			GlobalCaches.modEnabledCache.put(modId, ConfigFunctions.isModEnabled(FMLPaths.GAMEDIR.get().resolve("config"), modId));
		}
		return GlobalCaches.modEnabledCache.get(modId);
	}
}
