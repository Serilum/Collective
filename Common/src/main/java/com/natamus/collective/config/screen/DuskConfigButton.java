package com.natamus.collective.config.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DuskConfigButton extends Button {
	private final OnPress onRightPress;

	public DuskConfigButton(int x, int y, int width, int height, Component message, OnPress onPress) {
		this(x, y, width, height, message, onPress, null);
	}

	public DuskConfigButton(int x, int y, int width, int height, Component message, OnPress onPress, OnPress onRightPress) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
		this.onRightPress = onRightPress;
	}

	@Override
	protected boolean isValidClickButton(@NotNull MouseButtonInfo buttonInfo) {
		if (onRightPress != null && buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
			return true;
		}
		return super.isValidClickButton(buttonInfo);
	}

	@Override
	public void onClick(@NotNull MouseButtonEvent event, boolean doubleClick) {
		if (onRightPress != null && event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
			onRightPress.onPress(this);
			return;
		}
		super.onClick(event, doubleClick);
	}

	@Override
	protected void extractContents(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.extractDefaultSprite(guiGraphics);

		Font font = Minecraft.getInstance().font;
		int labelWidth = font.width(this.getMessage());
		int maxWidth = this.getWidth() - 6;
		if (labelWidth <= maxWidth) {
			this.extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
			return;
		}

		int colour = 0xFFA0A0A0;
		if (this.active) {
			colour = 0xFFFFFFFF;
		}

		float scale = maxWidth / (float) labelWidth;
		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(this.getX() + this.getWidth() / 2F, this.getY() + (this.getHeight() - 8 * scale) / 2F);
		guiGraphics.pose().scale(scale, scale);
		guiGraphics.centeredText(font, this.getMessage(), 0, 0, colour);
		guiGraphics.pose().popMatrix();
	}
}
