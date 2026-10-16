package com.natamus.collective.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.List;

public class DuskConfigIdList extends ContainerObjectSelectionList<DuskConfigIdRow> {
	public DuskConfigIdList(Minecraft minecraft, int x, int y, int width, int height) {
		super(minecraft, width, height, y, 24);
		this.centerListVertically = false;
		this.updateSizeAndPosition(width, height, x, y);
	}

	public void setRows(List<DuskConfigIdRow> rows) {
		double scroll = this.scrollAmount();
		this.replaceEntries(rows);
		this.setScrollAmount(scroll);
	}

	@Override
	public int getRowWidth() {
		return this.width - 12;
	}

	@Override
	protected int scrollBarX() {
		return this.getX() + this.width - 6;
	}
}
