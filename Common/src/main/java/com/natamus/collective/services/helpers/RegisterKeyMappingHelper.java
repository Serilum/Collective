package com.natamus.collective.services.helpers;

import net.minecraft.client.KeyMapping;

public interface RegisterKeyMappingHelper {
    KeyMapping registerKeyMapping(String description, int key, String category);
}