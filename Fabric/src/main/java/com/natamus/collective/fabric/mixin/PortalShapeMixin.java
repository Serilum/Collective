package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveBlockEvents;
import com.natamus.collective.functions.WorldFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PortalShape.class, priority = 1001)
public class PortalShapeMixin {
	@Shadow private @Final BlockPos bottomLeft;
	
	@Inject(method = "createPortalBlocks(Lnet/minecraft/world/level/LevelAccessor;)V", at = @At(value= "HEAD"))
	public void createPortalBlocks(LevelAccessor levelAccessor, CallbackInfo ci) {
		PortalShape portalshape = (PortalShape)(Object)this;
		CollectiveBlockEvents.ON_NETHER_PORTAL_SPAWN.invoker().onPossiblePortal(WorldFunctions.getWorldIfInstanceOfAndNotRemote(levelAccessor), bottomLeft, portalshape);
	}
}
