package com.natamus.collective.fabric.mixin;

import com.natamus.collective.worldcreation.WorldCreationGrid;
import net.minecraft.client.gui.layouts.GridLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = GridLayout.class, priority = 1001)
public class GridLayoutMixin implements WorldCreationGrid {
	@Shadow @Final private List<?> children;

	@Override
	public int collective$getNextRow() {
		int nextRow = 0;
		for (Object child : children) {
			nextRow = Math.max(nextRow, ((ChildContainerInvoker) child).collective$getLastOccupiedRow() + 1);
		}
		return nextRow;
	}
}
