package com.natamus.collective.worldcreation;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class WorldCreationSwitchRow extends WorldCreationRow {
	private final BooleanSupplier switchState;
	private final Consumer<Boolean> onSwitch;
	private CycleButton<Boolean> switchButton = null;

	public WorldCreationSwitchRow(WorldCreationTab tab, Component label, BooleanSupplier switchState, Consumer<Boolean> onSwitch) {
		super(tab, label);
		this.switchState = switchState;
		this.onSwitch = onSwitch;
	}

	@Override
	protected AbstractWidget createWidget(CreateWorldScreen screen) {
		CycleButton.Builder<Boolean> switchBuilder = CycleButton.onOffBuilder(switchState.getAsBoolean());
		if (tab == WorldCreationTab.WORLD) {
			switchBuilder.displayOnlyValue();
		}

		switchButton = switchBuilder.create(0, 0, getWidgetWidth(), 20, label, (cycleButton, value) -> onSwitch.accept(value));
		return switchButton;
	}

	@Override
	public void refresh() {
		if (switchButton != null) {
			switchButton.setValue(switchState.getAsBoolean());
		}
	}
}
