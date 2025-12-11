package com.natamus.collective.fabric.services;

import com.natamus.collective.data.ClientConstants;
import com.natamus.collective.services.helpers.RegisterKeyMappingHelper;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;

public class FabricRegisterKeyMappingHelper implements RegisterKeyMappingHelper {
	public @Nullable KeyMapping registerKeyMapping(String description, int key, String category) {
		if (!ClientConstants.keyMappingCategories.containsKey(category)) {
			return null;
		}

		return registerKeyMapping(description, key, ClientConstants.keyMappingCategories.get(category));
	}
	public KeyMapping registerKeyMapping(String description, int key, KeyMapping.Category keyMappingCategory) {
		return KeyBindingHelper.registerKeyBinding(new KeyMapping(description, key, keyMappingCategory));
	}
}