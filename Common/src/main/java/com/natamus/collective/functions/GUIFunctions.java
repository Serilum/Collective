package com.natamus.collective.functions;

import net.minecraft.client.Minecraft;

public class GUIFunctions {
    public static boolean shouldHideGUI() {
        return Minecraft.getInstance().gui.hud.getDebugOverlay().showDebugScreen() || Minecraft.getInstance().gui.hud.isHidden();
    }
}
