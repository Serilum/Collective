package com.natamus.collective.config.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (onRightPress != null && button == InputConstants.MOUSE_BUTTON_RIGHT && this.active && this.visible && this.isMouseOver(mouseX, mouseY)) {
			this.playDownSound(Minecraft.getInstance().getSoundManager());
			onRightPress.onPress(this);
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void renderString(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int colour) {
		int labelWidth = font.width(this.getMessage());
		int maxWidth = this.getWidth() - 6;
		if (labelWidth <= maxWidth) {
			super.renderString(guiGraphics, font, colour);
			return;
		}

		float scale = maxWidth / (float) labelWidth;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(this.getX() + this.getWidth() / 2F, this.getY() + (this.getHeight() - 8 * scale) / 2F, 0);
		guiGraphics.pose().scale(scale, scale, 1F);
		guiGraphics.drawCenteredString(font, this.getMessage(), 0, 0, colour);
		guiGraphics.pose().popPose();
	}
}
