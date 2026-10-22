package com.natamus.collective.forge.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.util.CollectiveReference;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.Optional;

public class ForgeCollectiveConfigScreen {
	public static void registerScreen(ModLoadingContext modLoadingContext) {
		registerScreen(modLoadingContext, CollectiveReference.MOD_ID);
	}

	public static void registerScreen(ModLoadingContext modLoadingContext, String modId) {
		Optional<? extends ModContainer> modContainer = ModList.get().getModContainerById(modId);
		if (modContainer.isPresent()) {
			registerScreen(modContainer.get(), modId);
		}
	}

	public static void registerScreen(ModContainer modContainer, String modId) {
		modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> {
			if (!DuskConfigModToggles.hasConfigScreen(modId)) {
				return null;
			}

			return new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> {
				return DuskConfig.DuskConfigScreen.getScreen(screen, modId);
			});
		});
	}
}
