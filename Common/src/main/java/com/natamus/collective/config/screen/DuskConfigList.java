package com.natamus.collective.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.List;

public class DuskConfigList extends ContainerObjectSelectionList<DuskConfigRow> {
	public DuskConfigList(Minecraft minecraft, int width, int height, int top, int bottom, int rowHeight) {
		super(minecraft, width, height, top, bottom, rowHeight);
		this.centerListVertically = false;

		if (minecraft.level != null) {
			this.setRenderBackground(false);
		}
	}

	public void setRows(List<DuskConfigRow> rows) {
		double scroll = this.getScrollAmount();
		this.replaceEntries(rows);
		this.setScrollAmount(scroll);
	}

	@Override
	public int getRowWidth() {
		return 10000;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 7;
	}
}
