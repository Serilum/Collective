package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.natamus.collective.fabric.callbacks.CollectiveBlockEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = Block.class, priority = 1001)
public class BlockMixin {
	@Inject(method = "setPlacedBy", at = @At(value = "HEAD"), cancellable = true) 
	public void Block_setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack, CallbackInfo ci) {
		if (!CollectiveBlockEvents.BLOCK_PLACE.invoker().onBlockPlace(level, blockPos, blockState, livingEntity, itemStack)) {
			ci.cancel();
		}
	}
	
	@Inject(method = "playerDestroy", at = @At(value = "HEAD"), cancellable = true) 
	public void Block_playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity, ItemStack itemStack, CallbackInfo ci) {
		if (!CollectiveBlockEvents.BLOCK_DESTROY.invoker().onBlockDestroy(level, player, blockPos, blockState, blockEntity, itemStack)) {
			ci.cancel();
		}
	}
}
