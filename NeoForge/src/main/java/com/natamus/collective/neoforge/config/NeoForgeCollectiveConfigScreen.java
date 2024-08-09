package com.natamus.collective.neoforge.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

public class NeoForgeCollectiveConfigScreen {
    public static void registerScreen(ModLoadingContext modLoadingContext) {
        modLoadingContext.registerExtensionPoint(IConfigScreenFactory.class, () -> new IConfigScreenFactory() {
            @Override
            public @NotNull Screen createScreen(@NotNull ModContainer modContainer, @NotNull Screen screen) {
                return DuskConfig.DuskConfigScreen.getScreen(screen, CollectiveReference.MOD_ID);
            }
        });
    }
}
