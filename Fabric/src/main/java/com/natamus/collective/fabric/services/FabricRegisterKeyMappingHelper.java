package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.RegisterKeyMappingHelper;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class FabricRegisterKeyMappingHelper implements RegisterKeyMappingHelper {
	public KeyMapping registerKeyMapping(String description, int key, String category) {
		return KeyBindingHelper.registerKeyBinding(new KeyMapping(description, key, category));
	}
}