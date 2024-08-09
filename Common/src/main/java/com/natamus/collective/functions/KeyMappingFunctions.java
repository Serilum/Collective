package com.natamus.collective.functions;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class KeyMappingFunctions {
    public static InputConstants.Key getKey(KeyMapping keyMapping) {
        return keyMapping.key;
    }
}