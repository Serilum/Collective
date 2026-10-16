package com.natamus.collective.config.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;

import java.util.List;

public class DuskConfigList extends ContainerObjectSelectionList<DuskConfigRow> {
	public DuskConfigList(Minecraft minecraft, int width, int height, int y, int rowHeight) {
		super(minecraft, width, height, y, rowHeight);
		this.centerListVertically = false;
	}

	public void setRows(List<DuskConfigRow> rows) {
		double scroll = this.getScrollAmount();
		this.replaceEntries(rows);
		this.setScrollAmount(scroll);
	}

	@Override
	protected boolean isValidMouseClick(int button) {
		return button == InputConstants.MOUSE_BUTTON_LEFT || button == InputConstants.MOUSE_BUTTON_RIGHT;
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
