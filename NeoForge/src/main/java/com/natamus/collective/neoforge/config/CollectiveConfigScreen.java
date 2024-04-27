package com.natamus.collective.neoforge.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.util.CollectiveReference;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class CollectiveConfigScreen {
    public static void registerScreen(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerExtensionPoint(IConfigScreenFactory.class, () -> (mc, screen) -> {
            return DuskConfig.DuskConfigScreen.getScreen(screen, CollectiveReference.MOD_ID);
        });
    }
}
