package com.natamus.collective.config.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DuskConfigDragArea extends AbstractWidget {
	private final BiConsumer<Float, Float> onPick;
	private final Consumer<GuiGraphicsExtractor> renderer;

	public DuskConfigDragArea(int x, int y, int width, int height, BiConsumer<Float, Float> onPick, Consumer<GuiGraphicsExtractor> renderer) {
		super(x, y, width, height, Component.empty());
		this.onPick = onPick;
		this.renderer = renderer;
	}

	@Override
	public void onClick(@NotNull MouseButtonEvent event, boolean doubleClick) {
		pick(event.x(), event.y());
	}

	@Override
	protected void onDrag(@NotNull MouseButtonEvent event, double dragX, double dragY) {
		pick(event.x(), event.y());
	}

	private void pick(double mouseX, double mouseY) {
		float fractionX = (float) Mth.clamp((mouseX - this.getX()) / (this.width - 1), 0, 1);
		float fractionY = (float) Mth.clamp((mouseY - this.getY()) / (this.height - 1), 0, 1);
		onPick.accept(fractionX, fractionY);
	}

	@Override
	protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderer.accept(guiGraphics);
	}

	@Override
	public void playDownSound(@NotNull SoundManager soundManager) { }

	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) { }
}
