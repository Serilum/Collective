package com.natamus.collective.fabric.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.natamus.collective.fabric.callbacks.CollectiveEntityEvents;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;

@Mixin(value = AbstractHorse.class, priority = 1001)
public class AbstractHorseMixin {
	@Inject(method = "tickRidden", at = @At(value= "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;setIsJumping(Z)V", ordinal = 0))
	public void AbstractHorse_travel(LivingEntity livingEntity, Vec3 vec3, CallbackInfo ci) {
		AbstractHorse horse = (AbstractHorse)(Object)this;
		CollectiveEntityEvents.ON_ENTITY_IS_JUMPING.invoker().onLivingJump(horse.level, horse);
	}
}
