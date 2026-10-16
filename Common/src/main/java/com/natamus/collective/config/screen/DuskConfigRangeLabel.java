package com.natamus.collective.config.screen;

import com.natamus.collective.config.DuskConfigEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DuskConfigRangeLabel extends AbstractWidget {
	private final String min;
	private final String max;

	public DuskConfigRangeLabel(int x, int width, DuskConfigEntry entry) {
		super(x, 0, width, 20, Component.empty());
		this.min = entry.getMinText();
		this.max = entry.getMaxText();
		this.active = false;
	}

	@Override
	protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		Font font = Minecraft.getInstance().font;

		guiGraphics.pose().pushMatrix();
		guiGraphics.pose().translate(this.getX(), this.getY());
		guiGraphics.pose().scale(0.85F, 0.85F);

		Component minLabel = Component.translatable("collective.collective.gui.min");
		Component maxLabel = Component.translatable("collective.collective.gui.max");
		int valueX = Math.max(font.width(minLabel), font.width(maxLabel)) + 5;

		guiGraphics.text(font, minLabel, 0, 2, 0xFF808080);
		guiGraphics.text(font, min, valueX, 2, 0xFFC0C0C0);
		guiGraphics.text(font, maxLabel, 0, 13, 0xFF808080);
		guiGraphics.text(font, max, valueX, 13, 0xFFC0C0C0);

		guiGraphics.pose().popMatrix();
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) { }
}
