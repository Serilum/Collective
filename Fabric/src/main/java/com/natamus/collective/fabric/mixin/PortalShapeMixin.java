package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.natamus.collective.fabric.callbacks.CollectiveBlockEvents;
import com.natamus.collective.functions.WorldFunctions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.PortalShape;

@Mixin(value = PortalShape.class, priority = 1001)
public class PortalShapeMixin {
	@Final @Shadow private LevelAccessor level;
	@Shadow private BlockPos bottomLeft;
	
	@Inject(method = "createPortalBlocks()V", at = @At(value= "HEAD"))
	public void createPortalBlocks(CallbackInfo ci) {
		PortalShape portalshape = (PortalShape)(Object)this;
		CollectiveBlockEvents.ON_NETHER_PORTAL_SPAWN.invoker().onPossiblePortal(WorldFunctions.getWorldIfInstanceOfAndNotRemote(level), bottomLeft, portalshape);
	}
}
