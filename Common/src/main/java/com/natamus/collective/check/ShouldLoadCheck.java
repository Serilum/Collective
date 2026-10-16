package com.natamus.collective.check;

import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.services.Services;

public class ShouldLoadCheck {
	public static boolean shouldLoad(String modId) {
		if (Services.MODLOADER.isJarJard(modId) && !Services.MODLOADER.isBundleModEnabled(modId)) {
			return false;
		}

		if (!ConfigFunctions.isModEnabled(modId)) {
			DuskConfigMod.addDisabledMod(modId);
			Services.MODLOADER.registerConfigScreen(modId);
			return false;
		}
		return true;
	}
}
