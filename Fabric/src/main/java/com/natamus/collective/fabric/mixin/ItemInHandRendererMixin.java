package com.natamus.collective.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.natamus.collective.fabric.callbacks.CollectiveRenderEvents;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemInHandRenderer.class, priority = 1001)
public class ItemInHandRendererMixin {
	@Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
	private void renderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
		if (!CollectiveRenderEvents.RENDER_SPECIFIC_HAND.invoker().onRenderSpecificHand(interactionHand, poseStack, itemStack)) {
			ci.cancel();
		}
	}
}
