package com.natamus.collective.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.List;

public class DuskConfigList extends ContainerObjectSelectionList<DuskConfigRow> {
	public DuskConfigList(Minecraft minecraft, int width, int height, int y, int rowHeight) {
		super(minecraft, width, height, y, rowHeight);
		this.centerListVertically = false;
	}

	public void setRows(List<DuskConfigRow> rows) {
		double scroll = this.scrollAmount();
		this.replaceEntries(rows);
		this.setScrollAmount(scroll);
	}

	@Override
	public int getRowWidth() {
		return 10000;
	}

	@Override
	protected int scrollBarX() {
		return this.width - 7;
	}
}
