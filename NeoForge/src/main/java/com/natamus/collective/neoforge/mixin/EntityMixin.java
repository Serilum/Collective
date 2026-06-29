package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.data.IEntityDataHolder;
import com.natamus.collective.services.helpers.EntityDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 1001)
public class EntityMixin implements IEntityDataHolder {
	@Unique private CompoundTag collective_stored = new CompoundTag();

	@Override
	public CompoundTag collective_getStored() {
		return collective_stored;
	}

	@Override
	public void collective_setStored(CompoundTag data) {
		collective_stored = data;
	}

	@Inject(method = "saveWithoutId(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "TAIL"))
	public void Entity_saveWithoutId(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
		if (!collective_stored.isEmpty()) {
			compound.put(EntityDataHelper.KEY, collective_stored);
		}
	}

	@Inject(method = "load(Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "TAIL"))
	public void Entity_load(CompoundTag compound, CallbackInfo ci) {
		collective_stored = compound.getCompound(EntityDataHelper.KEY);
	}
}
