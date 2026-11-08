package com.natamus.collective.config.screen;

import com.natamus.collective.config.DuskConfigEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class DuskConfigSetting {
	private final DuskConfigEditScreen screen;
	public final DuskConfigEntry entry;
	public final List<AbstractWidget> widgets = new ArrayList<>();
	private final AbstractWidget valueWidget;
	private final Button resetButton;
	private boolean resetPending = false;
	public boolean isValid = true;

	public DuskConfigSetting(DuskConfigEditScreen screen, DuskConfigEntry entry, int screenWidth) {
		this.screen = screen;
		this.entry = entry;

		int valueX = screenWidth - 160;
		int valueWidth = 150;
		if (entry.isText()) {
			widgets.add(Button.builder(Component.literal("..."), button -> Minecraft.getInstance().setScreen(new DuskConfigTextScreen(screen, entry))).pos(screenWidth - 30, 0).size(20, 20).build());
			valueWidth -= 25;
		}
		else if (entry.isRgb()) {
			widgets.add(new DuskConfigSwatch(screenWidth - 30, 0, 20, 20, entry::getRgbColour));
			valueWidth -= 25;
		}

		if (entry.hasRegistry() || entry.isRgb() || entry.isList() || entry.multiline) {
			valueWidget = new DuskConfigButton(valueX, 0, valueWidth, 20, getButtonLabel(), button -> openValueScreen());
		}
		else if (entry.isChatColour()) {
			valueWidget = new DuskConfigButton(valueX, 0, valueWidth, 20, getButtonLabel(), button -> openChatColourScreen());
		}
		else if (entry.isBoolean() || entry.hasOptions()) {
			valueWidget = new DuskConfigButton(valueX, 0, valueWidth, 20, getButtonLabel(), button -> cycleValue(false), button -> cycleValue(true));
		}
		else {
			int boxWidth = valueWidth;
			if (entry.hasRange()) {
				boxWidth = 75;
				widgets.add(new DuskConfigRangeLabel(screenWidth - 81, 71, entry));
			}

			EditBox editBox = new EditBox(Minecraft.getInstance().font, valueX, 0, boxWidth, 20, Component.translatable("options.generic_value"));
			editBox.setMaxLength(Integer.MAX_VALUE);
			editBox.setValue(entry.getText());
			editBox.setResponder(this::onTextChanged);
			valueWidget = editBox;
		}

		if (DuskConfigEditScreen.showTooltips && !entry.description.isEmpty()) {
			valueWidget.setTooltip(Tooltip.create(Component.literal(String.join(" ", entry.description))));
		}

		resetButton = new DuskConfigButton(screenWidth - 205, 0, 40, 20, getResetLabel(), button -> pressReset());
		updateResetButton();

		widgets.add(valueWidget);
		widgets.add(resetButton);
	}

	private Component getButtonLabel() {
		if (entry.hasRegistry()) {
			List<String> idList = entry.getIdList();
			if (idList.isEmpty()) {
				return Component.translatable("gui.none").withStyle(ChatFormatting.GRAY);
			}
			if (entry.single) {
				return Component.literal(DuskConfigRegistryIds.getName(entry.registry, idList.get(0)));
			}
			return DuskConfigRegistryIds.getAmountLabel(entry.registry, idList.size());
		}

		if (entry.isList()) {
			if (entry.getText().isEmpty()) {
				return Component.translatable("gui.none").withStyle(ChatFormatting.GRAY);
			}
			return Component.literal(entry.getText());
		}

		if (entry.multiline) {
			String text = entry.getText().strip();
			if (text.isEmpty()) {
				return Component.translatable("gui.none").withStyle(ChatFormatting.GRAY);
			}
			return Component.translatable("collective.collective.gui.lines", text.split("\\R").length);
		}

		if (entry.hasOptions()) {
			return Component.literal(entry.getOptionLabel(entry.getOption()));
		}

		if (entry.isRgb()) {
			return Component.literal(entry.getText().replace(",", ", "));
		}

		if (entry.isChatColour()) {
			return DuskConfigChatColourScreen.getColourLabel((Integer) entry.getValue());
		}

		if ((Boolean) entry.getValue()) {
			return Component.translatable("gui.yes").withStyle(ChatFormatting.GREEN);
		}
		return Component.translatable("gui.no").withStyle(ChatFormatting.RED);
	}

	private void openValueScreen() {
		if (entry.hasRegistry()) {
			Minecraft.getInstance().setScreen(new DuskConfigRegistryScreen(screen, entry));
		}
		else if (entry.isList() || entry.multiline) {
			Minecraft.getInstance().setScreen(new DuskConfigTextScreen(screen, entry));
		}
		else {
			Minecraft.getInstance().setScreen(new DuskConfigColourScreen(screen, entry));
		}
	}

	private void openChatColourScreen() {
		Component title = Component.literal(entry.getDisplayName());
		Minecraft.getInstance().setScreen(new DuskConfigChatColourScreen(screen, title, (Integer) entry.getValue(), entry::setValue));
	}

	private void cycleValue(boolean backwards) {
		if (entry.hasOptions()) {
			if (backwards) {
				entry.setOption(entry.getPreviousOption());
			}
			else {
				entry.setOption(entry.getNextOption());
			}
		}
		else {
			entry.setValue(!(Boolean) entry.getValue());
		}

		valueWidget.setMessage(getButtonLabel());
		screen.onSettingChanged();
	}

	private void onTextChanged(String text) {
		Object value = entry.getValueFromText(text);
		isValid = value != null;

		if (isValid) {
			entry.setValue(value);
			((EditBox) valueWidget).setTextColor(0xFFFFFFFF);
		}
		else {
			((EditBox) valueWidget).setTextColor(0xFFFF7777);
		}

		screen.onSettingChanged();
	}

	private Component getResetLabel() {
		if (resetPending) {
			return Component.literal("✔").withStyle(ChatFormatting.RED);
		}
		return Component.translatable("controls.reset").withStyle(ChatFormatting.RED);
	}

	private void pressReset() {
		if (!resetPending) {
			resetPending = true;
			resetButton.setMessage(getResetLabel());
			return;
		}

		cancelPendingReset();
		resetValue();
	}

	public boolean isResetPending() {
		return resetPending;
	}

	public void cancelPendingReset() {
		resetPending = false;
		resetButton.setMessage(getResetLabel());
	}

	private void resetValue() {
		entry.resetValue();

		if (valueWidget instanceof EditBox editBox) {
			editBox.setValue(entry.getText());
		}
		else {
			valueWidget.setMessage(getButtonLabel());
		}

		screen.onSettingChanged();
	}

	public void updateResetButton() {
		resetButton.active = !entry.isDefault();
	}
}
