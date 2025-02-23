package com.natamus.collective.functions;

import net.minecraft.client.Minecraft;

public class GUIFunctions {
    public static boolean shouldHideGUI() {
        return Minecraft.getInstance().options.renderDebug || Minecraft.getInstance().options.hideGui;
    }
}
