package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveFurnaceEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractFurnaceBlockEntity.class, priority = 1001)
public class AbstractFurnaceBlockEntityMixin {
	@Inject(method = "getBurnDuration", at = @At(value = "RETURN"), cancellable = true)
	public void AbstractFurnaceBlockEntity_getBurnDuration(ServerLevel level, ItemStack fuelItem, CallbackInfoReturnable<Integer> ci) {
		if (!fuelItem.isEmpty()) {
			Item item = fuelItem.getItem();
			int burntime = ci.getReturnValue();
			int newburntime = CollectiveFurnaceEvents.CALCULATE_FURNACE_BURN_TIME.invoker().getFurnaceBurnTime(fuelItem, burntime);
			if (burntime != newburntime) {
				ci.setReturnValue(newburntime);
			}
		}
	}
}
