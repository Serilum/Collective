package com.natamus.collective.functions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;

public class ScreenFunctions {
	public static void setScreenTitle(Screen screen, Component titleComponent) {
		screen.title = titleComponent;
	}

	public static void setMerchantScreenTitle(MerchantScreen merchantScreen, Component titleComponent) {
		merchantScreen.title = titleComponent;
	}
}
