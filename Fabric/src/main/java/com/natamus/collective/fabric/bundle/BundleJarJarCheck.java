package com.natamus.collective.fabric.bundle;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Optional;

public class BundleJarJarCheck {
	public static boolean isModJarJard(String modId) {
		boolean isJarJar = false;

		FabricLoader fabricLoaderInstance = FabricLoader.getInstance();
		if (fabricLoaderInstance != null) {
			Optional<ModContainer> modContainerOpt = fabricLoaderInstance.getModContainer(modId);

			if (modContainerOpt.isPresent()) {
				ModContainer modContainer = modContainerOpt.get();
				String location = modContainer.getOrigin().toString();

				isJarJar = location.contains("jarjar");
			}
		}

		return isJarJar;
	}
}
