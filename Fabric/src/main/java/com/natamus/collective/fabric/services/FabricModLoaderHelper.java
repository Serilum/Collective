package com.natamus.collective.fabric.services;

import com.natamus.collective.fabric.data.GlobalFabricObjects;
import com.natamus.collective.services.helpers.ModLoaderHelper;
import net.fabricmc.api.EnvType;

public class FabricModLoaderHelper implements ModLoaderHelper {
    @Override
    public String getModLoaderName() {
        return "Fabric";
    }

    @Override
    public String getGameDirectory() {
        return GlobalFabricObjects.fabricLoader.getGameDir().toString();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return GlobalFabricObjects.fabricLoader.isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return GlobalFabricObjects.fabricLoader.isDevelopmentEnvironment();
    }

    @Override
    public boolean isClientSide() {
        return GlobalFabricObjects.fabricLoader.getEnvironmentType() == EnvType.CLIENT;
    }
}
