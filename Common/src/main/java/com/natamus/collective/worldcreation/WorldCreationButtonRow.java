package com.natamus.collective.worldcreation;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WorldCreationButtonRow extends WorldCreationRow {
	private final Supplier<Component> valueText;
	private final Supplier<Component> tooltip;
	private final Consumer<CreateWorldScreen> onPress;
	private Button button = null;

	public WorldCreationButtonRow(WorldCreationTab tab, Component label, Supplier<Component> valueText, Supplier<Component> tooltip, Consumer<CreateWorldScreen> onPress) {
		this(tab, label, null, valueText, tooltip, onPress);
	}

	public WorldCreationButtonRow(WorldCreationTab tab, Component label, Component description, Supplier<Component> valueText, Supplier<Component> tooltip, Consumer<CreateWorldScreen> onPress) {
		super(tab, label, description);
		this.valueText = valueText;
		this.tooltip = tooltip;
		this.onPress = onPress;
	}

	@Override
	protected AbstractWidget createWidget(CreateWorldScreen screen) {
		button = Button.builder(getButtonMessage(), pressedButton -> onPress.accept(screen)).width(getWidgetWidth()).build();
		refreshTooltip();
		return button;
	}

	@Override
	public void refresh() {
		if (button == null) {
			return;
		}

		button.setMessage(getButtonMessage());
		refreshTooltip();
	}

	private Component getButtonMessage() {
		if (tab == WorldCreationTab.WORLD) {
			return valueText.get();
		}
		return CommonComponents.optionNameValue(label, valueText.get());
	}

	private void refreshTooltip() {
		if (tooltip != null) {
			button.setTooltip(Tooltip.create(tooltip.get()));
		}
		else if (tab != WorldCreationTab.WORLD) {
			button.setTooltip(getDescriptionTooltip());
		}
	}
}
