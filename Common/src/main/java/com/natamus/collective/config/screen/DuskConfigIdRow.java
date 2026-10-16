package com.natamus.collective.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuskConfigIdRow extends ContainerObjectSelectionList.Entry<DuskConfigIdRow> {
	private final ItemStack icon;
	private final String name;
	private final String id;
	private final int colour;
	private final Button button;

	public DuskConfigIdRow(ItemStack icon, String name, String id, int colour, Component buttonLabel, Button.OnPress onPress) {
		this.icon = icon;
		this.name = name;
		this.id = id;
		this.colour = colour;
		this.button = Button.builder(buttonLabel, onPress).size(20, 20).build();
	}

	@Override
	public void extractContent(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		int x = this.getContentX();
		int y = this.getContentY();
		int right = this.getContentRight();

		if (!icon.isEmpty()) {
			guiGraphics.item(icon, x - 2, y + 2);
		}

		Font font = Minecraft.getInstance().font;
		int labelWidth = right - x - 48;
		if (name.isEmpty()) {
			guiGraphics.text(font, DuskConfigRow.fitLabel(font, id, labelWidth), x + 20, y + 6, colour);
		}
		else {
			guiGraphics.text(font, DuskConfigRow.fitLabel(font, name, labelWidth), x + 20, y + 1, colour);

			guiGraphics.pose().pushMatrix();
			guiGraphics.pose().translate(x + 23, y + 12);
			guiGraphics.pose().scale(0.75F, 0.75F);
			guiGraphics.text(font, DuskConfigRow.fitLabel(font, id, (int) (labelWidth / 0.75F)), 0, 0, 0xFF909090);
			guiGraphics.pose().popMatrix();
		}

		button.setX(right - 24);
		button.setY(y);
		button.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
	}

	@Override
	public @NotNull List<? extends GuiEventListener> children() {
		return List.of(button);
	}

	@Override
	public @NotNull List<? extends NarratableEntry> narratables() {
		return List.of(button);
	}
}
