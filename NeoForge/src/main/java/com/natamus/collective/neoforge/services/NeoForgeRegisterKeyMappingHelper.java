package com.natamus.collective.neoforge.services;

import com.natamus.collective.services.helpers.RegisterKeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeRegisterKeyMappingHelper implements RegisterKeyMappingHelper {
	private static final List<KeyMapping> keyMappingsToRegister = new ArrayList<>();

	public KeyMapping registerKeyMapping(String description, int key, String category) {
		KeyMapping keyMapping = new KeyMapping(description, key, category);

		keyMappingsToRegister.add(keyMapping);

		return keyMapping;
	}

	public static void registerKeyMappings(final RegisterKeyMappingsEvent e) {
		for (KeyMapping keyMapping : keyMappingsToRegister) {
			e.register(keyMapping);
		}
    }
}