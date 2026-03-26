package com.natamus.collective.neoforge.services;

import com.natamus.collective.data.ClientConstants;
import com.natamus.collective.services.helpers.RegisterKeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NeoForgeRegisterKeyMappingHelper implements RegisterKeyMappingHelper {
	private static final List<KeyMapping> keyMappingsToRegister = new ArrayList<>();

	public @Nullable KeyMapping registerKeyMapping(String description, int key, String category) {
		if (!ClientConstants.keyMappingCategories.containsKey(category)) {
			return null;
		}

		return registerKeyMapping(description, key, ClientConstants.keyMappingCategories.get(category));
	}
	public KeyMapping registerKeyMapping(String description, int key, KeyMapping.Category keyMappingCategory) {
		KeyMapping keyMapping = new KeyMapping(description, key, keyMappingCategory);

		keyMappingsToRegister.add(keyMapping);

		return keyMapping;
	}

	public static void registerKeyMappings(final RegisterKeyMappingsEvent e) {
		for (KeyMapping keyMapping : keyMappingsToRegister) {
			e.register(keyMapping);
		}
    }
}