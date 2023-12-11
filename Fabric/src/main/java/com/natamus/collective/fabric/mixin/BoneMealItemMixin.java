package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.natamus.collective.fabric.callbacks.CollectiveCropEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

@Mixin(value = BoneMealItem.class, priority = 1001)
public class BoneMealItemMixin {
	@Inject(method = "useOn", at = @At(value = "HEAD"), cancellable = true)
	public void BoneMealItem_useOn(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> ci) {
		Level level = useOnContext.getLevel();
		BlockPos blockPos = useOnContext.getClickedPos();

		if (!CollectiveCropEvents.ON_BONE_MEAL_APPLY.invoker().onBoneMealApply(useOnContext.getPlayer(), level, blockPos, level.getBlockState(blockPos), useOnContext.getItemInHand())) {
			ci.setReturnValue(InteractionResult.PASS);
		}
	}

	@Inject(method = "growCrop(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z", at = @At(value = "HEAD"))
	private static void growCrop(ItemStack itemStack, Level level, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
		CollectiveCropEvents.ON_GENERAL_BONE_MEAL_APPLY.invoker().onGeneralBoneMealApply(level, blockPos, level.getBlockState(blockPos), itemStack);
	}
}
