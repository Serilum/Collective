package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.layouts.GridLayout$CellInhabitant", priority = 1001)
public interface CellInhabitantInvoker {
	@Invoker("getLastOccupiedRow")
	int collective$getLastOccupiedRow();
}
