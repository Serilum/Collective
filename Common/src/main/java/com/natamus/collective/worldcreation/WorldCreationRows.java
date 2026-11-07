package com.natamus.collective.worldcreation;

import com.natamus.collective.config.DuskConfigValueEntry;
import com.natamus.collective.config.screen.DuskConfigRegistryIds;
import com.natamus.collective.config.screen.DuskConfigRegistryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class WorldCreationRows {
	private static final List<WorldCreationRow> rows = new CopyOnWriteArrayList<>();

	public static void addButtonRow(WorldCreationTab tab, Component label, Supplier<Component> valueText, Supplier<Component> tooltip, Consumer<CreateWorldScreen> onPress) {
		addButtonRow(tab, label, null, valueText, tooltip, onPress);
	}

	public static void addButtonRow(WorldCreationTab tab, Component label, Component description, Supplier<Component> valueText, Supplier<Component> tooltip, Consumer<CreateWorldScreen> onPress) {
		rows.add(new WorldCreationButtonRow(tab, label, description, valueText, tooltip, onPress));
	}

	public static void addSwitchRow(WorldCreationTab tab, Component label, BooleanSupplier switchState, Consumer<Boolean> onSwitch) {
		addSwitchRow(tab, label, null, switchState, onSwitch);
	}

	public static void addSwitchRow(WorldCreationTab tab, Component label, Component description, BooleanSupplier switchState, Consumer<Boolean> onSwitch) {
		rows.add(new WorldCreationSwitchRow(tab, label, description, switchState, onSwitch));
	}

	public static void addSwitchRow(WorldCreationTab tab, Component label, Component description, Predicate<WorldCreationUiState> activeCondition, Component inactiveDescription, BooleanSupplier switchState, Consumer<Boolean> onSwitch) {
		rows.add(new WorldCreationSwitchRow(tab, label, description, activeCondition, inactiveDescription, switchState, onSwitch));
	}

	public static void openRegistryPicker(Screen parent, String title, String registry, boolean weights, String value, Consumer<String> onChange) {
		Consumer<String> onChangeAndRefresh = newValue -> {
			onChange.accept(newValue);
			refreshRows();
		};

		if (parent instanceof CreateWorldScreen createWorldScreen) {
			useRegistriesOf(createWorldScreen);
		}

		DuskConfigValueEntry pickerEntry = new DuskConfigValueEntry(title, registry, weights, value, onChangeAndRefresh);
		Minecraft.getInstance().setScreen(new DuskConfigRegistryScreen(parent, pickerEntry));
	}

	public static void refreshRows() {
		for (WorldCreationRow row : rows) {
			row.refresh();
		}
	}

	public static void addRowsToTabs(CreateWorldScreen screen, Tab[] tabs) {
		if (rows.isEmpty()) {
			return;
		}

		useRegistriesOf(screen);
		screen.getUiState().addListener(uiState -> refreshRows());

		for (Tab tab : tabs) {
			WorldCreationTab worldCreationTab = WorldCreationTab.fromTitle(tab.getTabTitle());
			if (worldCreationTab != null && tab instanceof WorldCreationTabLayout tabLayout) {
				addRowsToGrid(screen, worldCreationTab, tabLayout.collective$getLayout());
			}
		}
	}

	private static void useRegistriesOf(CreateWorldScreen screen) {
		DuskConfigRegistryIds.worldCreationRegistries = screen.getUiState().getSettings().worldgenLoadContext();
	}

	private static void addRowsToGrid(CreateWorldScreen screen, WorldCreationTab tab, GridLayout grid) {
		int nextRow = ((WorldCreationGrid) grid).collective$getNextRow();
		for (WorldCreationRow row : rows) {
			if (row.tab != tab) {
				continue;
			}

			grid.addChild(row.createElement(screen), nextRow, 0, 1, tab.columns, getCellSettings(grid, tab));
			nextRow += 1;
		}
	}

	private static LayoutSettings getCellSettings(GridLayout grid, WorldCreationTab tab) {
		if (tab == WorldCreationTab.WORLD) {
			return grid.newCellSettings().paddingTop(-4);
		}
		return grid.newCellSettings();
	}

	public static void forgetCreateWorldScreen() {
		DuskConfigRegistryIds.worldCreationRegistries = null;
	}
}
