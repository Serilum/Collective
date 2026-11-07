package com.natamus.collective.worldcreation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;

public abstract class WorldCreationRow {
	private static final int tabButtonWidth = 210;
	private static final int switchButtonWidth = 44;
	private static final int worldTabWidth = 310;

	public final WorldCreationTab tab;
	protected final Component label;
	protected final Component description;
	private StringWidget labelWidget = null;

	protected WorldCreationRow(WorldCreationTab tab, Component label) {
		this(tab, label, null);
	}

	protected WorldCreationRow(WorldCreationTab tab, Component label, Component description) {
		this.tab = tab;
		this.label = label;
		this.description = description;
	}

	protected abstract AbstractWidget createWidget(CreateWorldScreen screen);

	public abstract void refresh();

	public LayoutElement createElement(CreateWorldScreen screen) {
		AbstractWidget widget = createWidget(screen);
		if (tab != WorldCreationTab.WORLD) {
			return widget;
		}
		return createLabeledRow(widget);
	}

	protected int getWidgetWidth() {
		if (tab == WorldCreationTab.WORLD) {
			return switchButtonWidth;
		}
		return tabButtonWidth;
	}

	protected Component getDescription() {
		return description;
	}

	protected Tooltip getDescriptionTooltip() {
		Component shownDescription = getDescription();
		if (shownDescription == null) {
			return null;
		}
		return Tooltip.create(shownDescription);
	}

	protected void refreshLabelTooltip() {
		if (labelWidget != null) {
			labelWidget.setTooltip(getDescriptionTooltip());
		}
	}

	private GridLayout createLabeledRow(AbstractWidget widget) {
		GridLayout rowLayout = new GridLayout();
		rowLayout.addChild(SpacerElement.width(worldTabWidth), 0, 0, 1, 2);
		rowLayout.addChild(createLabelWidget(), 1, 0, rowLayout.newCellSettings().align(0.0F, 0.5F));
		rowLayout.addChild(widget, 1, 1, rowLayout.newCellSettings().alignHorizontallyRight());
		return rowLayout;
	}

	private StringWidget createLabelWidget() {
		labelWidget = new StringWidget(label, Minecraft.getInstance().font);
		labelWidget.setTooltip(getDescriptionTooltip());
		return labelWidget;
	}
}
