package com.natamus.collective.functions;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

import java.util.HashMap;
import java.util.Map;

public class KeyMappingFunctions {
	private static final Map<KeyMapping, Boolean> keyMappingsHeldLastTick = new HashMap<>();

	public static InputConstants.Key getKey(KeyMapping keyMapping) {
		return keyMapping.key;
	}

	public static boolean consumePress(KeyMapping keyMapping) {
		boolean wasClicked = false;
		while (keyMapping.consumeClick()) {
			wasClicked = true;
		}

		boolean wasHeldLastTick = keyMappingsHeldLastTick.getOrDefault(keyMapping, false);
		keyMappingsHeldLastTick.put(keyMapping, keyMapping.isDown());

		return wasClicked && !wasHeldLastTick;
	}
}
