package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveAttackEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BowItem.class, priority = 1001)
public class BowItemMixin {
	@Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasInfiniteMaterials()Z"), cancellable = true)
	public void use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack itemStack = player.getItemInHand(interactionHand);

		InteractionResult interactionResult = CollectiveAttackEvents.ON_ARROW_NOCK.invoker().onArrowNock(itemStack, level, player, interactionHand, !player.getProjectile(itemStack).isEmpty());
		if (!interactionResult.equals(InteractionResult.PASS)) {
			cir.setReturnValue(interactionResult);
		}
	}
}