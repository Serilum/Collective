package com.natamus.collective.forge.mixin;

import com.natamus.collective.features.PlayerPlacedBlocksFeature;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockItem.class, priority = 1001)
public class BlockItemMixin {
	@Inject(method = "placeBlock", at = @At(value = "RETURN"))
	public void BlockItem_placeBlock(BlockPlaceContext context, BlockState placementState, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			PlayerPlacedBlocksFeature.onBlockPlaced(context.getLevel(), context.getClickedPos(), context.getPlayer());
		}
	}
}
