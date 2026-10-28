package com.natamus.collective.neoforge.services;


import com.natamus.collective.neoforge.bundle.NeoForgeBundleConfigCheck;
import com.natamus.collective.neoforge.bundle.NeoForgeBundleJarJarCheck;
import com.natamus.collective.neoforge.config.NeoForgeCollectiveConfigScreen;
import com.natamus.collective.services.helpers.ModLoaderHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModFileInfo;

import java.io.IOException;
import java.util.Optional;

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
	public String getModName(String modId) {
		Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
		if (modContainer.isEmpty()) {
			return modId;
		}
		return modContainer.get().getModInfo().getDisplayName();
	}

	@Override
	public byte[] getModIcon(String modId) {
		IModFileInfo modFileInfo = ModList.get().getModFileById(modId);
		if (modFileInfo == null) {
			return null;
		}

		try {
			return modFileInfo.getFile().getContents().readFile("icon.png");
		}
		catch (IOException ignored) { }
		return null;
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLEnvironment.isProduction();
	}

	@Override
	public boolean isClientSide() {
		return FMLEnvironment.getDist().equals(Dist.CLIENT);
	}

	@Override
	public boolean isJarJard(String modId) {
		return NeoForgeBundleJarJarCheck.isModJarJard(modId);
	}

	@Override
	public boolean isBundleModEnabled(String modId) {
		return NeoForgeBundleConfigCheck.isBundleModEnabled(modId);
	}

	@Override
	public void registerConfigScreen(String modId) {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		if (modId.equals(modLoadingContext.getActiveNamespace())) {
			NeoForgeCollectiveConfigScreen.registerScreen(modLoadingContext, modId);
		}
	}
}