package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.worldcreation.WorldCreationGrid;
import net.minecraft.client.gui.layouts.GridLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = GridLayout.class, priority = 1001)
public class GridLayoutMixin implements WorldCreationGrid {
	@Shadow @Final private List<?> cellInhabitants;

	@Override
	public int collective$getNextRow() {
		int nextRow = 0;
		for (Object cellInhabitant : cellInhabitants) {
			nextRow = Math.max(nextRow, ((CellInhabitantInvoker) cellInhabitant).collective$getLastOccupiedRow() + 1);
		}
		return nextRow;
	}
}
