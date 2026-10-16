package com.natamus.collective.config.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntSupplier;

public class DuskConfigSwatch extends AbstractWidget {
	private final IntSupplier colourSupplier;

	public DuskConfigSwatch(int x, int y, int width, int height, IntSupplier colourSupplier) {
		super(x, y, width, height, Component.empty());
		this.colourSupplier = colourSupplier;
		this.active = false;
	}

	@Override
	protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		int x = this.getX();
		int y = this.getY();

		guiGraphics.fill(x, y, x + this.width, y + this.height, 0xFF000000);
		guiGraphics.fill(x + 1, y + 1, x + this.width - 1, y + this.height - 1, 0xFF000000 | colourSupplier.getAsInt());
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) { }
}
