package com.natamus.collective.services.helpers;

import net.minecraft.client.KeyMapping;

public interface RegisterKeyMappingHelper {
    KeyMapping registerKeyMapping(String description, int key, String category);
    KeyMapping registerKeyMapping(String description, int key, KeyMapping.Category keyMappingCategory);
}