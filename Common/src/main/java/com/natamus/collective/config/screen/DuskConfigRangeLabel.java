package com.natamus.collective.config.screen;

import com.natamus.collective.config.DuskConfigEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
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
	protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		Font font = Minecraft.getInstance().font;

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(this.getX(), this.getY(), 0);
		guiGraphics.pose().scale(0.85F, 0.85F, 1F);

		Component minLabel = Component.translatable("collective.collective.gui.min");
		Component maxLabel = Component.translatable("collective.collective.gui.max");
		int valueX = Math.max(font.width(minLabel), font.width(maxLabel)) + 5;

		guiGraphics.drawString(font, minLabel, 0, 2, 0xFF808080);
		guiGraphics.drawString(font, min, valueX, 2, 0xFFC0C0C0);
		guiGraphics.drawString(font, maxLabel, 0, 13, 0xFF808080);
		guiGraphics.drawString(font, max, valueX, 13, 0xFFC0C0C0);

		guiGraphics.pose().popPose();
	}

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) { }
}
