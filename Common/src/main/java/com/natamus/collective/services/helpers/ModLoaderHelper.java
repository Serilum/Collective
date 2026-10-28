package com.natamus.collective.services.helpers;

public interface ModLoaderHelper {
	String getModLoaderName();
	String getGameDirectory();
	boolean isModLoaded(String modId);
	String getModName(String modId);
	byte[] getModIcon(String modId);
	boolean isDevelopmentEnvironment();
	boolean isClientSide();
	boolean isJarJard(String modId);
	boolean isBundleModEnabled(String modId);
	void registerConfigScreen(String modId);
}