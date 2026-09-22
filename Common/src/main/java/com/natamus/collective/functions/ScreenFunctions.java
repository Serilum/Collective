package com.natamus.collective.functions;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;

import java.util.List;

public class ScreenFunctions {
	public static void setScreenTitle(Screen screen, Component titleComponent) {
		screen.title = titleComponent;
	}

	public static void setMerchantScreenTitle(MerchantScreen merchantScreen, Component titleComponent) {
		merchantScreen.title = titleComponent;
	}

	// Sign Edit Screen
	public static void signSetMessage(AbstractSignEditScreen abstractSignEditScreen, String newMessage, int newLine, boolean setLine) {
		if (setLine) {
			abstractSignEditScreen.line = newLine;
		}

		abstractSignEditScreen.messages[newLine] = newMessage;
		abstractSignEditScreen.text.setLine(newLine, Component.literal(newMessage));
		abstractSignEditScreen.sign.setText(abstractSignEditScreen.text.asImmutable(), abstractSignEditScreen.slot);
	}

	public static SignBlockEntity getSignBlockEntityFromScreen(AbstractSignEditScreen abstractSignEditScreen) {
		return abstractSignEditScreen.sign;
	}

	public static SignText getSignTextFromScreen(AbstractSignEditScreen abstractSignEditScreen) {
		return abstractSignEditScreen.text.asImmutable();
	}
	public static void setSignTextOnScreen(AbstractSignEditScreen abstractSignEditScreen, String message, int line) {
		abstractSignEditScreen.text.setLine(line, Component.literal(message));
	}
	public static void setSignTextOnScreen(AbstractSignEditScreen abstractSignEditScreen, Component message, int line) {
		abstractSignEditScreen.text.setLine(line, message);
	}
	public static void replaceSignTextOnScreen(AbstractSignEditScreen abstractSignEditScreen, SignText signText) {
		List<Component> messages = signText.getMessages(false);
		List<Component> filteredMessages = signText.getMessages(true);
		for (int i = 0; i < messages.size(); i++) {
			abstractSignEditScreen.text.setLine(i, messages.get(i), filteredMessages.get(i));
		}

		abstractSignEditScreen.text.setColor(signText.getColor()).setTextGlowing(signText.hasGlowingText());
	}

	public static String[] getSignMessagesFromScreen(AbstractSignEditScreen abstractSignEditScreen) {
		return abstractSignEditScreen.messages;
	}
	public static void setSignMessagesOnScreen(AbstractSignEditScreen abstractSignEditScreen, String[] newMessages) {
		int i = 0;
		for (String message : newMessages) {
			abstractSignEditScreen.messages[i] = message;
			i+=1;
		}
	}
	public static void setSignMessageAtLineOnScreen(AbstractSignEditScreen abstractSignEditScreen, String newMessage, int line) {
		abstractSignEditScreen.messages[line] = newMessage;
	}

	public static int getSignLineFromScreen(AbstractSignEditScreen abstractSignEditScreen) {
		return abstractSignEditScreen.line;
	}
	public static void setSignLineOnScreen(AbstractSignEditScreen abstractSignEditScreen, int newLine) {
		abstractSignEditScreen.line = newLine;
	}

	public static TextFieldHelper getSignFieldFromScreen(AbstractSignEditScreen abstractSignEditScreen) {
		return abstractSignEditScreen.signField;
	}

	public static boolean signTextOnScreenisFront(AbstractSignEditScreen abstractSignEditScreen) {
		return abstractSignEditScreen.slot == SignTextSlot.FRONT;
	}

	public static <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(Screen screen, T renderableWidget) {
		return screen.addRenderableWidget(renderableWidget);
	}
}
