package com.natamus.collective.config.screen;

import com.natamus.collective.config.DuskConfigEntry;
import com.natamus.collective.functions.ColourFunctions;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DuskConfigColourScreen extends DuskConfigBaseScreen {
	private static final int areaSize = 120;

	private final Screen parent;
	private final DuskConfigEntry entry;
	private final int originalColour;
	private int colour;
	private float hue;
	private float saturation;
	private float brightness;
	private int areaX;
	private int hueBarX;
	private int fieldsX;
	private EditBox redBox;
	private EditBox greenBox;
	private EditBox blueBox;
	private EditBox hexBox;
	private boolean updatingBoxes = false;

	public DuskConfigColourScreen(Screen parent, DuskConfigEntry entry) {
		super(Component.literal(entry.getDisplayName()));
		this.parent = parent;
		this.entry = entry;

		int entryColour = entry.getRgbColour();
		if (entryColour == -1) {
			entryColour = 0xFFFFFF;
		}
		this.originalColour = entryColour;
		setColour(entryColour);
	}

	@Override
	protected void init() {
		super.init();

		areaX = this.width / 2 - 130;
		hueBarX = areaX + areaSize + 8;
		fieldsX = hueBarX + 28;

		this.addRenderableWidget(new DuskConfigDragArea(areaX, 40, areaSize, areaSize, (x, y) -> {
			saturation = x;
			brightness = 1 - y;
			colour = getColourFromHsv(hue, saturation, brightness);
			updateChannelBoxes();
			updateHexBox();
		}, this::drawSaturationBrightness));
		this.addRenderableWidget(new DuskConfigDragArea(hueBarX, 40, 12, areaSize, (x, y) -> {
			hue = y;
			colour = getColourFromHsv(hue, saturation, brightness);
			updateChannelBoxes();
			updateHexBox();
		}, this::drawHueBar));

		this.addRenderableWidget(new DuskConfigSwatch(fieldsX, 50, 50, 20, () -> colour));
		this.addRenderableWidget(new DuskConfigSwatch(fieldsX + 54, 50, 50, 20, () -> originalColour));

		redBox = addChannelBox(86, "color.minecraft.red");
		greenBox = addChannelBox(110, "color.minecraft.green");
		blueBox = addChannelBox(134, "color.minecraft.blue");

		hexBox = this.addRenderableWidget(new EditBox(this.font, fieldsX + 20, 158, 64, 20, Component.translatable("collective.collective.gui.hex")));
		hexBox.setMaxLength(7);
		hexBox.setResponder(text -> onHexChanged());

		updateChannelBoxes();
		updateHexBox();

		addCancelButton(this.width / 2 - 154, this.height - 28);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			entry.setValue(ColourFunctions.getRgbString(colour));
			onClose();
		}).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build());
	}

	@Override
	protected boolean hasUnsavedChanges() {
		return colour != originalColour;
	}

	private EditBox addChannelBox(int y, String colourKey) {
		EditBox channelBox = this.addRenderableWidget(new EditBox(this.font, fieldsX + 20, y, 40, 20, Component.translatable(colourKey)));
		channelBox.setMaxLength(3);
		channelBox.setResponder(text -> onChannelChanged());
		return channelBox;
	}

	private void onChannelChanged() {
		if (updatingBoxes) {
			return;
		}

		int rgbColour = ColourFunctions.getRgbColour(redBox.getValue() + "," + greenBox.getValue() + "," + blueBox.getValue());
		if (rgbColour == -1) {
			return;
		}

		setColour(rgbColour);
		updateHexBox();
	}

	private void onHexChanged() {
		if (updatingBoxes) {
			return;
		}

		String hex = hexBox.getValue().strip().replace("#", "");
		if (!hex.matches("[0-9a-fA-F]{6}")) {
			return;
		}

		setColour(Integer.parseInt(hex, 16));
		updateChannelBoxes();
	}

	private void updateChannelBoxes() {
		updatingBoxes = true;
		redBox.setValue(String.valueOf((colour >> 16) & 0xFF));
		greenBox.setValue(String.valueOf((colour >> 8) & 0xFF));
		blueBox.setValue(String.valueOf(colour & 0xFF));
		updatingBoxes = false;
	}

	private void updateHexBox() {
		updatingBoxes = true;
		hexBox.setValue(String.format(Locale.ROOT, "#%06X", colour));
		updatingBoxes = false;
	}

	private void setColour(int rgbColour) {
		colour = rgbColour;

		float red = ((rgbColour >> 16) & 0xFF) / 255F;
		float green = ((rgbColour >> 8) & 0xFF) / 255F;
		float blue = (rgbColour & 0xFF) / 255F;
		float max = Math.max(red, Math.max(green, blue));
		float delta = max - Math.min(red, Math.min(green, blue));

		brightness = max;
		saturation = 0;
		if (max > 0) {
			saturation = delta / max;
		}

		if (delta == 0) {
			return;
		}

		if (max == red) {
			hue = ((green - blue) / delta) / 6;
		}
		else if (max == green) {
			hue = ((blue - red) / delta + 2) / 6;
		}
		else {
			hue = ((red - green) / delta + 4) / 6;
		}

		if (hue < 0) {
			hue += 1;
		}
	}

	private static int getColourFromHsv(float hue, float saturation, float brightness) {
		return Mth.hsvToRgb(Math.min(hue, 0.999F), saturation, brightness) & 0xFFFFFF;
	}

	private void drawSaturationBrightness(GuiGraphics guiGraphics) {
		for (int column = 0; column < areaSize; column++) {
			int topColour = 0xFF000000 | getColourFromHsv(hue, column / (float) (areaSize - 1), 1);
			guiGraphics.fillGradient(areaX + column, 40, areaX + column + 1, 40 + areaSize, topColour, 0xFF000000);
		}

		int markerX = areaX + Math.round(saturation * (areaSize - 1));
		int markerY = 40 + Math.round((1 - brightness) * (areaSize - 1));
		drawMarker(guiGraphics, markerX - 2, markerY - 2, markerX + 3, markerY + 3);
	}

	private void drawHueBar(GuiGraphics guiGraphics) {
		for (int row = 0; row < areaSize; row++) {
			guiGraphics.fill(hueBarX, 40 + row, hueBarX + 12, 41 + row, 0xFF000000 | getColourFromHsv(row / (float) (areaSize - 1), 1, 1));
		}

		int markerY = 40 + Math.round(hue * (areaSize - 1));
		drawMarker(guiGraphics, hueBarX - 1, markerY - 1, hueBarX + 13, markerY + 2);
	}

	private static void drawMarker(GuiGraphics guiGraphics, int left, int top, int right, int bottom) {
		guiGraphics.fill(left, top, right, top + 1, 0xFFFFFFFF);
		guiGraphics.fill(left, bottom - 1, right, bottom, 0xFFFFFFFF);
		guiGraphics.fill(left, top, left + 1, bottom, 0xFFFFFFFF);
		guiGraphics.fill(right - 1, top, right, bottom, 0xFFFFFFFF);
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
		guiGraphics.drawString(this.font, Component.translatable("collective.collective.gui.new"), fieldsX, 40, 0xFFA0A0A0);
		guiGraphics.drawString(this.font, Component.translatable("collective.collective.gui.old"), fieldsX + 54, 40, 0xFFA0A0A0);
		guiGraphics.drawString(this.font, getInitial("color.minecraft.red"), fieldsX, 92, 0xFFA0A0A0);
		guiGraphics.drawString(this.font, getInitial("color.minecraft.green"), fieldsX, 116, 0xFFA0A0A0);
		guiGraphics.drawString(this.font, getInitial("color.minecraft.blue"), fieldsX, 140, 0xFFA0A0A0);
		guiGraphics.drawString(this.font, Component.translatable("collective.collective.gui.hex"), fieldsX - 4, 164, 0xFFA0A0A0);
	}

	private static String getInitial(String colourKey) {
		String colourName = Component.translatable(colourKey).getString();
		return colourName.substring(0, colourName.offsetByCodePoints(0, 1));
	}
}
