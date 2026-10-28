package com.natamus.collective.check;

import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.services.Services;

public class ShouldLoadCheck {
	public static boolean shouldLoad(String modId) {
		if (Services.MODLOADER.isJarJard(modId) && !Services.MODLOADER.isBundleModEnabled(modId)) {
			return false;
		}

		DuskConfigModToggles.addKnownMod(modId);
		Services.MODLOADER.registerConfigScreen(modId);

		if (!ConfigFunctions.isModEnabled(modId)) {
			DuskConfigMod.addDisabledMod(modId);
			return false;
		}
		return true;
	}
}
