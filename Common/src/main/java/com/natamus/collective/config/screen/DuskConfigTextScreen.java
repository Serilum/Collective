package com.natamus.collective.config.screen;

import com.natamus.collective.config.DuskConfigEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DuskConfigTextScreen extends DuskConfigBaseScreen {
	private final Screen parent;
	private final DuskConfigEntry entry;
	private List<FormattedCharSequence> descriptionLines = new ArrayList<>();
	private String text;
	private int boxX;

	public DuskConfigTextScreen(Screen parent, DuskConfigEntry entry) {
		super(Component.literal(entry.getDisplayName()));
		this.parent = parent;
		this.entry = entry;
		this.text = getEntryText();
	}

	private String getEntryText() {
		if (entry.isList()) {
			return entry.getListLines();
		}
		return entry.getText();
	}

	@Override
	protected void init() {
		super.init();

		int boxWidth = Math.min(this.width - 40, 400);
		boxX = (this.width - boxWidth) / 2;

		descriptionLines = this.font.split(FormattedText.of(String.join(" ", entry.description)), boxWidth);
		int boxTop = 34 + descriptionLines.size() * 10;

		MultiLineEditBox textBox = new MultiLineEditBox(this.font, boxX, boxTop, boxWidth, this.height - boxTop - 40, Component.empty(), Component.literal(entry.getDisplayName()));
		textBox.setCharacterLimit(Integer.MAX_VALUE);
		textBox.setValue(text);
		textBox.setValueListener(newText -> text = newText);
		this.addRenderableWidget(textBox);
		this.setInitialFocus(textBox);

		addCancelButton(this.width / 2 - 154, this.height - 28);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			if (entry.isList()) {
				entry.setListFromLines(text);
			}
			else {
				entry.setValue(text.replaceAll("\\R", " "));
			}
			onClose();
		}).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build());
	}

	@Override
	protected boolean hasUnsavedChanges() {
		return !text.equals(getEntryText());
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}

	@Override
	public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (this.minecraft.level == null) {
			this.renderPanorama(guiGraphics, partialTick);
		}

		this.renderBlurredBackground(partialTick);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);

		int y = 30;
		for (FormattedCharSequence line : descriptionLines) {
			guiGraphics.drawString(this.font, line, boxX, y, 0xFFA0A0A0);
			y += 10;
		}
	}
}
