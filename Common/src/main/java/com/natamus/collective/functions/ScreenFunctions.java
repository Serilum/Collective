package com.natamus.collective.functions;

import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class ScreenFunctions {
	public static void setScreenTitle(Screen screen, Component titleComponent) {
		screen.title = titleComponent;
	}

	public static void setMerchantScreenTitle(MerchantScreen merchantScreen, Component titleComponent) {
		merchantScreen.title = titleComponent;
	}

	// Sign Edit Screen
    public static void signSetMessage(SignEditScreen signEditScreen, String newMessage, int newLine, boolean setLine) {
		if (setLine) {
			signEditScreen.line = newLine;
		}

        signEditScreen.messages[newLine] = newMessage;
        signEditScreen.sign.setMessage(newLine, Component.literal(newMessage));
    }

	public static SignBlockEntity getSignBlockEntityFromScreen(SignEditScreen signEditScreen) {
		return signEditScreen.sign;
	}

	public static String[] getSignMessagesFromScreen(SignEditScreen signEditScreen) {
		return signEditScreen.messages;
	}
	public static void setSignMessagesOnScreen(SignEditScreen signEditScreen, String[] newMessages) {
		int i = 0;
		for (String message : newMessages) {
			signEditScreen.messages[i] = message;
			i+=1;
		}
	}
	public static void setSignMessageAtLineOnScreen(SignEditScreen signEditScreen, String newMessage, int line) {
		signEditScreen.messages[line] = newMessage;
	}

	public static int getSignLineFromScreen(SignEditScreen signEditScreen) {
		return signEditScreen.line;
	}
	public static void setSignLineOnScreen(SignEditScreen signEditScreen, int newLine) {
		signEditScreen.line = newLine;
	}

	public static TextFieldHelper getSignFieldFromScreen(SignEditScreen signEditScreen) {
		return signEditScreen.signField;
	}
}
