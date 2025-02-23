package com.natamus.collective.functions;

import net.minecraft.client.Minecraft;

public class GUIFunctions {
    public static boolean shouldHideGUI() {
        return Minecraft.getInstance().gui.getDebugOverlay().showDebugScreen() || Minecraft.getInstance().options.hideGui;
    }
}
