package com.natamus.collective.fabric.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.util.CollectiveReference;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class FabricCollectiveConfigScreen implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> DuskConfig.DuskConfigScreen.getScreen(parent, CollectiveReference.MOD_ID);
    }
}