package com.natamus.collective.check;

import com.natamus.collective.bundle.BundleConfigCheck;
import com.natamus.collective.services.Services;

public class ShouldLoadCheck {
	public static boolean shouldLoad(String modId) {
		if (Services.MODLOADER.isJarJard(modId)) {
			return BundleConfigCheck.isBundleModEnabled(modId);
		}
		return true;
	}
}
