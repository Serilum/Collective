package com.natamus.collective.worldcreation;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WorldCreationSwitchRow extends WorldCreationRow {
	private final Predicate<WorldCreationUiState> activeCondition;
	private final Component inactiveDescription;
	private final BooleanSupplier switchState;
	private final Consumer<Boolean> onSwitch;
	private CycleButton<Boolean> switchButton = null;
	private WorldCreationUiState uiState = null;

	public WorldCreationSwitchRow(WorldCreationTab tab, Component label, BooleanSupplier switchState, Consumer<Boolean> onSwitch) {
		this(tab, label, null, switchState, onSwitch);
	}

	public WorldCreationSwitchRow(WorldCreationTab tab, Component label, Component description, BooleanSupplier switchState, Consumer<Boolean> onSwitch) {
		this(tab, label, description, worldCreationUiState -> true, null, switchState, onSwitch);
	}

	public WorldCreationSwitchRow(WorldCreationTab tab, Component label, Component description, Predicate<WorldCreationUiState> activeCondition, Component inactiveDescription, BooleanSupplier switchState, Consumer<Boolean> onSwitch) {
		super(tab, label, description);
		this.activeCondition = activeCondition;
		this.inactiveDescription = inactiveDescription;
		this.switchState = switchState;
		this.onSwitch = onSwitch;
	}

	@Override
	protected AbstractWidget createWidget(CreateWorldScreen screen) {
		uiState = screen.getUiState();

		CycleButton.Builder<Boolean> switchBuilder = CycleButton.onOffBuilder(getShownState());
		switchBuilder.withTooltip(value -> getDescriptionTooltip());
		if (tab == WorldCreationTab.WORLD) {
			switchBuilder.displayOnlyValue();
		}

		switchButton = switchBuilder.create(0, 0, getWidgetWidth(), 20, label, (cycleButton, value) -> onSwitch.accept(value));
		switchButton.active = isActive();
		return switchButton;
	}

	@Override
	public void refresh() {
		if (switchButton == null) {
			return;
		}

		switchButton.setValue(getShownState());
		switchButton.active = isActive();
		refreshLabelTooltip();
	}

	@Override
	protected Component getDescription() {
		if (!isActive() && inactiveDescription != null) {
			return inactiveDescription;
		}
		return description;
	}

	private boolean getShownState() {
		if (!isActive()) {
			return false;
		}
		return switchState.getAsBoolean();
	}

	private boolean isActive() {
		return activeCondition.test(uiState);
	}
}
