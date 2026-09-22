package com.natamus.collective.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.natamus.collective.fabric.callbacks.CollectiveRenderEvents;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FirstPersonHandsAndItemsRenderer.class, priority = 1001)
public class FirstPersonHandsAndItemsRendererMixin {
	@Inject(method = "submitArmWithItem", at = @At(value = "HEAD"), cancellable = true)
	private void submitArmWithItem(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
		if (!CollectiveRenderEvents.RENDER_SPECIFIC_HAND.invoker().onRenderSpecificHand(hand, poseStack, itemStack)) {
			ci.cancel();
		}
	}
}
