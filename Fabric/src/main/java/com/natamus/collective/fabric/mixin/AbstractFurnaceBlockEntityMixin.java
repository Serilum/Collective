package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveFurnaceEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractFurnaceBlockEntity.class, priority = 1001)
public class AbstractFurnaceBlockEntityMixin {
	@Inject(method = "getBurnDuration", at = @At(value = "HEAD"), cancellable = true)
	public void AbstractFurnaceBlockEntity_getBurnDuration(FuelValues fuelValues, ItemStack itemStack, CallbackInfoReturnable<Integer> ci) {
		if (!itemStack.isEmpty()) {
			Item item = itemStack.getItem();
			int burntime = fuelValues.burnDuration(itemStack);
			int newburntime = CollectiveFurnaceEvents.CALCULATE_FURNACE_BURN_TIME.invoker().getFurnaceBurnTime(itemStack, burntime);
			if (burntime != newburntime) {
				ci.setReturnValue(newburntime);
			}
		}
	}
}
