package com.natamus.collective.functions;

import net.minecraft.client.Minecraft;

public class GUIFunctions {
    public static boolean shouldHideGUI() {
        return Minecraft.getInstance().debugEntries.isOverlayVisible() || Minecraft.getInstance().options.hideGui;
    }
}
