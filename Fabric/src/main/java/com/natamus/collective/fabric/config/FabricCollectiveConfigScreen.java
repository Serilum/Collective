package com.natamus.collective.fabric.config;

import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.util.CollectiveReference;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import java.util.HashMap;
import java.util.Map;

public class FabricCollectiveConfigScreen implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> DuskConfig.DuskConfigScreen.getScreen(parent, CollectiveReference.MOD_ID);
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		Map<String, ConfigScreenFactory<?>> factories = new HashMap<>();
		for (String modid : DuskConfigModToggles.getKnownModIds()) {
			if (DuskConfigModToggles.hasConfigScreen(modid)) {
				factories.put(modid, parent -> DuskConfig.DuskConfigScreen.getScreen(parent, modid));
			}
		}
		return factories;
	}
}
