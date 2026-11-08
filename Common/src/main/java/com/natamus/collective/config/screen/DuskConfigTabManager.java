package com.natamus.collective.config.screen;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;

import java.util.function.Consumer;

public class DuskConfigTabManager extends TabManager {
	private final Consumer<Tab> onSelected;

	public DuskConfigTabManager(Consumer<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget, Consumer<Tab> onSelected) {
		super(addWidget, removeWidget);
		this.onSelected = onSelected;
	}

	@Override
	public void setCurrentTab(Tab tab, boolean playSound) {
		super.setCurrentTab(tab, playSound);
		onSelected.accept(tab);
	}
}
