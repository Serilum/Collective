package com.natamus.collective.services.helpers;

import com.natamus.collective.data.GlobalVariables;

public interface ModLoaderHelper {
	String getModLoaderName();
	String getGameDirectory();
	boolean isModLoadedUncached(String modId);
	String getModName(String modId);
	byte[] getModIcon(String modId);
	boolean isDevelopmentEnvironment();
	boolean isClientSide();
	boolean isJarJard(String modId);
	boolean isBundleModEnabled(String modId);
	void registerConfigScreen(String modId);

	default boolean isModLoaded(String modId) {
		return isModLoaded(modId, false);
	}

	default boolean isModLoaded(String modId, boolean refreshCache) {
		if (refreshCache) {
			GlobalVariables.loadedModCache.remove(modId);
		}
		return GlobalVariables.loadedModCache.computeIfAbsent(modId, this::isModLoadedUncached);
	}
}