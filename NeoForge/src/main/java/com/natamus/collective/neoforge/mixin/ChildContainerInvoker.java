package com.natamus.collective.neoforge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.gui.layouts.GridLayout$ChildContainer", priority = 1001)
public interface ChildContainerInvoker {
	@Invoker("getLastOccupiedRow")
	int collective$getLastOccupiedRow();
}
