package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.natamus.collective.fabric.callbacks.CollectiveFurnaceEvents;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@Mixin(value = AbstractFurnaceBlockEntity.class, priority = 1001)
public class AbstractFurnaceBlockEntityMixin {
	@Inject(method = "getBurnDuration", at = @At(value = "HEAD"), cancellable = true)
	public void AbstractFurnaceBlockEntity_getBurnDuration(ItemStack itemStack, CallbackInfoReturnable<Integer> ci) {
		if (!itemStack.isEmpty()) {
			Item item = itemStack.getItem();
			int burntime = AbstractFurnaceBlockEntity.getFuel().getOrDefault(item, 0);
			int newburntime = CollectiveFurnaceEvents.CALCULATE_FURNACE_BURN_TIME.invoker().getFurnaceBurnTime(itemStack, burntime);
			if (burntime != newburntime) {
				ci.setReturnValue(newburntime);
			}
		}
	}
}
