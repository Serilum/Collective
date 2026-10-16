package com.natamus.collective.config.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DuskConfigDragArea extends AbstractWidget {
	private final BiConsumer<Float, Float> onPick;
	private final Consumer<GuiGraphics> renderer;

	public DuskConfigDragArea(int x, int y, int width, int height, BiConsumer<Float, Float> onPick, Consumer<GuiGraphics> renderer) {
		super(x, y, width, height, Component.empty());
		this.onPick = onPick;
		this.renderer = renderer;
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		pick(mouseX, mouseY);
	}

	@Override
	protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
		pick(mouseX, mouseY);
	}

	private void pick(double mouseX, double mouseY) {
		float fractionX = (float) Mth.clamp((mouseX - this.getX()) / (this.width - 1), 0, 1);
		float fractionY = (float) Mth.clamp((mouseY - this.getY()) / (this.height - 1), 0, 1);
		onPick.accept(fractionX, fractionY);
	}

	@Override
	protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderer.accept(guiGraphics);
	}

	@Override
	public void playDownSound(@NotNull SoundManager soundManager) { }

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) { }
}
