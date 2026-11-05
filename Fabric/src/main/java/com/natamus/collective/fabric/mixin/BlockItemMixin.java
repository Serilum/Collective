package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveBlockEvents;
import com.natamus.collective.features.PlayerPlacedBlocksFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockItem.class, priority = 1001)
public class BlockItemMixin {
	@Inject(method = "place", at = @At(value = "HEAD"), cancellable = true)
	public void Block_setPlacedBy(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		Level level = context.getLevel();
		BlockPos clickedPos = context.getClickedPos();

		if (!CollectiveBlockEvents.BLOCK_PLACE.invoker().onBlockPlace(level, clickedPos, level.getBlockState(clickedPos), context.getPlayer(), context.getItemInHand())) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Inject(method = "placeBlock", at = @At(value = "RETURN"))
	public void BlockItem_placeBlock(BlockPlaceContext context, BlockState placementState, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) {
			PlayerPlacedBlocksFeature.onBlockPlaced(context.getLevel(), context.getClickedPos(), context.getPlayer());
		}
	}
}
