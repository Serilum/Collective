package com.natamus.collective.fabric.callbacks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class CollectiveRenderEvents {
	private CollectiveRenderEvents() { }

	public static final Event<Render_Specific_Hand> RENDER_SPECIFIC_HAND = EventFactory.createArrayBacked(Render_Specific_Hand.class, callbacks -> (interactionHand, poseStack, itemStack) -> {
		for (Render_Specific_Hand callback : callbacks) {
			if (!callback.onRenderSpecificHand(interactionHand, poseStack, itemStack)) {
				return false;
			}
		}

		return true;
	});

	@FunctionalInterface
	public interface Render_Specific_Hand {
		boolean onRenderSpecificHand(InteractionHand interactionHand, PoseStack poseStack, ItemStack itemStack);
	}
}
