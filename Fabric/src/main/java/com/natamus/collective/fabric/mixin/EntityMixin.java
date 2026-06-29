package com.natamus.collective.fabric.mixin;

import com.natamus.collective.data.IEntityDataHolder;
import com.natamus.collective.services.helpers.EntityDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

	@Inject(method = "saveWithoutId(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At(value = "TAIL"))
	public void Entity_saveWithoutId(ValueOutput output, CallbackInfo ci) {
		if (!collective_stored.isEmpty()) {
			output.store(EntityDataHelper.KEY, CompoundTag.CODEC, collective_stored);
		}
	}

	@Inject(method = "load(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At(value = "TAIL"))
	public void Entity_load(ValueInput input, CallbackInfo ci) {
		collective_stored = input.read(EntityDataHelper.KEY, CompoundTag.CODEC).orElse(new CompoundTag());
	}
}
