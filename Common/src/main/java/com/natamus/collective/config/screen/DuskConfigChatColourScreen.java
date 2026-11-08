package com.natamus.collective.config.screen;

import com.natamus.collective.functions.ColourFunctions;
import com.natamus.collective.functions.StringFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntConsumer;

public class DuskConfigChatColourScreen extends DuskConfigBaseScreen {
	private static final int columns = 4;
	private static final int rows = 4;
	private static final int buttonWidth = 100;
	private static final int buttonHeight = 20;
	private static final int gap = 4;

	private final Screen parent;
	private final int currentColourId;
	private final IntConsumer onPick;

	public DuskConfigChatColourScreen(Screen parent, Component title, int currentColourId, IntConsumer onPick) {
		super(title);
		this.parent = parent;
		this.currentColourId = currentColourId;
		this.onPick = onPick;
	}

	public static Component getColourLabel(int colourId) {
		ChatFormatting colour = ColourFunctions.getById(colourId);
		if (colour == null || colour == ChatFormatting.RESET) {
			return Component.literal(String.valueOf(colourId));
		}
		return Component.literal(StringFunctions.capitalizeEveryWord(colour.name().replace("_", " "))).withStyle(colour);
	}

	@Override
	protected void init() {
		super.init();

		int gridWidth = columns * buttonWidth + (columns - 1) * gap;
		int gridHeight = rows * buttonHeight + (rows - 1) * gap;
		int left = this.width / 2 - gridWidth / 2;
		int top = this.height / 2 - gridHeight / 2;

		for (int colourId = 0; colourId < columns * rows; colourId++) {
			int x = left + (colourId % columns) * (buttonWidth + gap);
			int y = top + (colourId / columns) * (buttonHeight + gap);
			int pickedColourId = colourId;
			this.addRenderableWidget(Button.builder(getButtonLabel(colourId), button -> pick(pickedColourId)).pos(x, y).size(buttonWidth, buttonHeight).build());
		}

		addCancelButton(this.width / 2 - 75, this.height - 28);
	}

	private Component getButtonLabel(int colourId) {
		if (colourId != currentColourId) {
			return getColourLabel(colourId);
		}

		MutableComponent label = Component.literal("> ");
		label.append(getColourLabel(colourId));
		label.append(" <");
		return label;
	}

	private void pick(int colourId) {
		onPick.accept(colourId);
		onClose();
	}

	@Override
	protected boolean hasUnsavedChanges() {
		return false;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}

	@Override
	public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (this.minecraft.level == null) {
			this.renderPanorama(guiGraphics, partialTick);
		}

		this.renderBlurredBackground(partialTick);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
	}
}
