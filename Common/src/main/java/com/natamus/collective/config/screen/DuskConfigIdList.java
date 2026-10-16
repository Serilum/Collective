package com.natamus.collective.config.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.List;

public class DuskConfigIdList extends ContainerObjectSelectionList<DuskConfigIdRow> {
	public DuskConfigIdList(Minecraft minecraft, int x, int y, int width, int height) {
		super(minecraft, width, height, y, y + height, 24);
		this.centerListVertically = false;
		this.setLeftPos(x);
		this.setRenderTopAndBottom(false);

		if (minecraft.level != null) {
			this.setRenderBackground(false);
		}
	}

	public void setRows(List<DuskConfigIdRow> rows) {
		double scroll = this.getScrollAmount();
		this.replaceEntries(rows);
		this.setScrollAmount(scroll);
	}

	@Override
	public int getRowWidth() {
		return this.width - 12;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.x0 + this.width - 6;
	}
}
