package com.natamus.collective.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuskConfigIdRow extends ContainerObjectSelectionList.Entry<DuskConfigIdRow> {
	private final ItemStack icon;
	private final DuskConfigModIcons modIcon;
	private final Identifier effectSprite;
	private final String name;
	private final String id;
	private final int colour;
	private final Button button;
	private final EditBox amountBox;

	public DuskConfigIdRow(ItemStack icon, DuskConfigModIcons modIcon, Identifier effectSprite, String name, String id, int colour, Component buttonLabel, Button.OnPress onPress) {
		this(icon, modIcon, effectSprite, name, id, colour, buttonLabel, onPress, null);
	}
	public DuskConfigIdRow(ItemStack icon, DuskConfigModIcons modIcon, Identifier effectSprite, String name, String id, int colour, Component buttonLabel, Button.OnPress onPress, EditBox amountBox) {
		this.icon = icon;
		this.modIcon = modIcon;
		this.effectSprite = effectSprite;
		this.name = name;
		this.id = id;
		this.colour = colour;
		this.button = Button.builder(buttonLabel, onPress).size(20, 20).build();
		this.amountBox = amountBox;
	}

	@Override
	public void extractContent(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
		int x = this.getContentX();
		int y = this.getContentY();
		int right = this.getContentRight();

		if (!icon.isEmpty()) {
			guiGraphics.item(icon, x - 2, y + 2);
		}
		else if (modIcon != null) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, modIcon.texture, x - 2, y + 2, 0, 0, 16, 16, modIcon.width, modIcon.height, modIcon.width, modIcon.height);
		}
		else if (effectSprite != null) {
			guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, effectSprite, x - 2, y + 2, 16, 16);
		}

		Font font = Minecraft.getInstance().font;
		int labelWidth = right - x - 48;
		if (amountBox != null) {
			labelWidth -= amountBox.getWidth() + 4;
		}
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

		if (amountBox != null) {
			amountBox.setX(right - 28 - amountBox.getWidth());
			amountBox.setY(y);
			amountBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	@Override
	public @NotNull List<? extends GuiEventListener> children() {
		if (amountBox != null) {
			return List.of(amountBox, button);
		}
		return List.of(button);
	}

	@Override
	public @NotNull List<? extends NarratableEntry> narratables() {
		if (amountBox != null) {
			return List.of(amountBox, button);
		}
		return List.of(button);
	}
}
