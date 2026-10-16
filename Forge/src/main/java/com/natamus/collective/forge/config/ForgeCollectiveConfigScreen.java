package com.natamus.collective.forge.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.util.CollectiveReference;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class ForgeCollectiveConfigScreen {
	public static void registerScreen(ModLoadingContext modLoadingContext) {
		registerScreen(modLoadingContext, CollectiveReference.MOD_ID);
	}

	public static void registerScreen(ModLoadingContext modLoadingContext, String modId) {
		modLoadingContext.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> {
			return new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> {
				return DuskConfig.DuskConfigScreen.getScreen(screen, modId);
			});
		});
	}
}
