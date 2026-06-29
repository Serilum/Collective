package com.natamus.collective.fabric.mixin;

import com.natamus.collective.data.BlockEntityData;
import com.natamus.collective.globalcallbacks.CachedBlockEntityCallback;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockEntity.class, priority = 1001)
public class BlockEntityMixin {
	@Shadow private @Final BlockEntityType<?> type;
	@Shadow protected Level level;

	@Inject(method = "setLevel(Lnet/minecraft/world/level/Level;)V", at = @At(value = "TAIL"))
	public void setLevel(Level level, CallbackInfo ci) {
		if (level == null) {
			return;
		}

		if (!BlockEntityData.shouldCacheOnSide(type, level.isClientSide())) {
			return;
		}

		BlockEntity blockEntity = (BlockEntity)(Object)this;

		BlockEntityData.cacheBlockEntity(type, level, blockEntity);
		CachedBlockEntityCallback.BLOCK_ENTITY_ADDED.invoker().onBlockEntityAdded(level, blockEntity, type);
	}

	@Inject(method = "setRemoved()V", at = @At(value = "TAIL"))
	public void setRemoved(CallbackInfo ci) {
		if (level == null) {
			return;
		}

		if (!BlockEntityData.shouldCacheOnSide(type, level.isClientSide())) {
			return;
		}

		BlockEntity blockEntity = (BlockEntity)(Object)this;

		BlockEntityData.uncacheBlockEntity(type, level, blockEntity);
		CachedBlockEntityCallback.BLOCK_ENTITY_REMOVED.invoker().onBlockEntityRemoved(level, blockEntity, type);
	}
}
