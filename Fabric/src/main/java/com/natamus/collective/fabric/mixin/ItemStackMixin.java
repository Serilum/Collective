package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveItemEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemStack.class, priority = 1001)
public abstract class ItemStackMixin {
	@Unique private static final ThreadLocal<ItemStack> COLLECTIVE$PROCESSED_STACK = new ThreadLocal<>();

	@Inject(method = "finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "HEAD"))
	public void finishUsingItemA(Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
		if (livingEntity instanceof Player) {
			ItemStack itemStack = (ItemStack) (Object) this;
			COLLECTIVE$PROCESSED_STACK.set(itemStack.copy());
		}
	}

	@Inject(method = "finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "RETURN"), cancellable = true)
	public void finishUsingItemB(Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
		if (livingEntity instanceof Player) {
			InteractionHand hand = livingEntity.getUsedItemHand();

			ItemStack copyStack = COLLECTIVE$PROCESSED_STACK.get();
			ItemStack newStack = cir.getReturnValue();
			ItemStack changedStack = CollectiveItemEvents.ON_ITEM_USE_FINISHED.invoker().onItemUsedFinished((Player) livingEntity, copyStack, newStack, hand);

			if (changedStack != null) {
				cir.setReturnValue(changedStack);
			}
		}
	}
}
