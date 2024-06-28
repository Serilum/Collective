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

import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(value = BlockEntity.class, priority = 1001)
public class BlockEntityMixin {
	@Shadow private @Final BlockEntityType<?> type;
	@Shadow protected Level level;

	@Inject(method = "setLevel(Lnet/minecraft/world/level/Level;)V", at = @At(value = "TAIL"))
	public void setLevel(Level level, CallbackInfo ci) {
		if (BlockEntityData.blockEntitiesToCache.contains(type)) {
			if (level != null) {
				if (!BlockEntityData.cachedBlockEntities.get(type).containsKey(level)) {
					BlockEntityData.cachedBlockEntities.get(type).put(level, new CopyOnWriteArrayList<BlockEntity>());
				}

				BlockEntity blockEntity = (BlockEntity)(Object)this;

				BlockEntityData.cachedBlockEntities.get(type).get(level).add(blockEntity);
				CachedBlockEntityCallback.BLOCK_ENTITY_ADDED.invoker().onBlockEntityAdded(level, blockEntity, type);
			}
		}
	}

	@Inject(method = "setRemoved()V", at = @At(value = "TAIL"))
	public void setRemoved(CallbackInfo ci) {
		if (BlockEntityData.blockEntitiesToCache.contains(type)) {
			if (level != null) {
				if (BlockEntityData.cachedBlockEntities.get(type).containsKey(level)) {
					BlockEntity blockEntity = (BlockEntity)(Object)this;

					BlockEntityData.cachedBlockEntities.get(type).get(level).remove(blockEntity);
					CachedBlockEntityCallback.BLOCK_ENTITY_REMOVED.invoker().onBlockEntityRemoved(level, blockEntity, type);
				}
			}
		}
	}
}
