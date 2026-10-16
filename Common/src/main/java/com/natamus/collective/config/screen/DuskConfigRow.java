package com.natamus.collective.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuskConfigRow extends ContainerObjectSelectionList.Entry<DuskConfigRow> {
	private final String label;
	private final List<AbstractWidget> widgets;

	public DuskConfigRow(String label, List<AbstractWidget> widgets) {
		this.label = label;
		this.widgets = widgets;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
		int labelSpace = Integer.MAX_VALUE;
		for (AbstractWidget widget : widgets) {
			widget.setY(y);
			widget.render(guiGraphics, mouseX, mouseY, partialTick);
			labelSpace = Math.min(labelSpace, widget.getX() - 18);
		}

		Font font = Minecraft.getInstance().font;
		guiGraphics.drawString(font, fitLabel(font, label, labelSpace), 12, y + 5, 0xFFFFFFFF);
	}

	public static String fitLabel(Font font, String label, int width) {
		if (font.width(label) <= width) {
			return label;
		}
		return font.plainSubstrByWidth(label, Math.max(0, width - font.width("..."))) + "...";
	}

	@Override
	public @NotNull List<? extends GuiEventListener> children() {
		return widgets;
	}

	@Override
	public @NotNull List<? extends NarratableEntry> narratables() {
		return widgets;
	}
}
