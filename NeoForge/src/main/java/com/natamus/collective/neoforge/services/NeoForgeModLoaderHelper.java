package com.natamus.collective.neoforge.services;


import com.natamus.collective.neoforge.bundle.NeoForgeBundleJarJarCheck;
import com.natamus.collective.services.helpers.ModLoaderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

public class NeoForgeModLoaderHelper implements ModLoaderHelper {
    @Override
    public String getModLoaderName() {
        return "NeoForge";
    }

    @Override
    public String getGameDirectory() {
        return FMLPaths.GAMEDIR.get().toString();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isClientSide() {
        return FMLEnvironment.dist.equals(Dist.CLIENT);
    }

    @Override
    public boolean isJarJard(String modId) {
        return NeoForgeBundleJarJarCheck.isModJarJard(modId);
    }
}