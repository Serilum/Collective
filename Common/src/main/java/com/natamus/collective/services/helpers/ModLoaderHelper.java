package com.natamus.collective.services.helpers;

public interface ModLoaderHelper {
    String getModLoaderName();
    String getGameDirectory();
    boolean isModLoaded(String modId);
    boolean isDevelopmentEnvironment();
    boolean isClientSide();
    boolean isJarJard(String modId);
}