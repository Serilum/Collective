package com.natamus.collective.fabric.services;

import com.natamus.collective.fabric.bundle.FabricBundleConfigCheck;
import com.natamus.collective.fabric.bundle.FabricBundleJarJarCheck;
import com.natamus.collective.fabric.data.GlobalFabricObjects;
import com.natamus.collective.services.helpers.ModLoaderHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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
	public String getModName(String modId) {
		Optional<ModContainer> modContainer = GlobalFabricObjects.fabricLoader.getModContainer(modId);
		if (modContainer.isEmpty()) {
			return modId;
		}
		return modContainer.get().getMetadata().getName();
	}

	@Override
	public byte[] getModIcon(String modId) {
		Optional<ModContainer> modContainer = GlobalFabricObjects.fabricLoader.getModContainer(modId);
		if (modContainer.isEmpty()) {
			return null;
		}

		Optional<Path> iconPath = modContainer.get().findPath("icon.png");
		if (iconPath.isEmpty() || !Files.isRegularFile(iconPath.get())) {
			return null;
		}

		try {
			return Files.readAllBytes(iconPath.get());
		}
		catch (IOException ignored) { }
		return null;
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return GlobalFabricObjects.fabricLoader.isDevelopmentEnvironment();
	}

	@Override
	public boolean isClientSide() {
		return GlobalFabricObjects.fabricLoader.getEnvironmentType() == EnvType.CLIENT;
	}

	@Override
	public boolean isJarJard(String modId) {
		return FabricBundleJarJarCheck.isModJarJard(modId);
	}

	@Override
	public boolean isBundleModEnabled(String modId) {
		return FabricBundleConfigCheck.isBundleModEnabled(modId);
	}

	@Override
	public void registerConfigScreen(String modId) {

	}
}
