package com.natamus.collective.forge.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.util.CollectiveReference;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class CollectiveConfigScreen {
    public static void registerScreen(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> {
            return new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> {
                return DuskConfig.DuskConfigScreen.getScreen(screen, CollectiveReference.MOD_ID);
            });
        });
    }
}
