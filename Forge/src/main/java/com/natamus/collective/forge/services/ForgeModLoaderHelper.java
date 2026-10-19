package com.natamus.collective.forge.services;

import com.natamus.collective.forge.bundle.ForgeBundleConfigCheck;
import com.natamus.collective.forge.bundle.ForgeBundleJarJarCheck;
import com.natamus.collective.forge.config.ForgeCollectiveConfigScreen;
import com.natamus.collective.services.helpers.ModLoaderHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModFileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ForgeModLoaderHelper implements ModLoaderHelper {
	@Override
	public String getModLoaderName() {
		return "Forge";
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

		Path iconPath = modFileInfo.getFile().findResource("icon.png");
		if (!Files.isRegularFile(iconPath)) {
			return null;
		}

		try {
			return Files.readAllBytes(iconPath);
		}
		catch (IOException ignored) { }
		return null;
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
		return ForgeBundleJarJarCheck.isModJarJard(modId);
	}

	@Override
	public boolean isBundleModEnabled(String modId) {
		return ForgeBundleConfigCheck.isBundleModEnabled(modId);
	}

	@Override
	public void registerConfigScreen(String modId) {
		Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
		if (modContainer.isPresent()) {
			ForgeCollectiveConfigScreen.registerScreen(modContainer.get(), modId);
		}
	}
}