package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.natamus.collective.fabric.callbacks.CollectiveExplosionEvents;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

@Mixin(value = Explosion.class, priority = 1001)
public class ExplosionMixin {
	@Final @Shadow private Level level;
	@Final @Shadow private Entity source;
	
	@Inject(method = "explode()V", at = @At(value = "TAIL"))
	public void Explosion_explode(CallbackInfo ci) {
		Explosion explosion = (Explosion)(Object)this;
		CollectiveExplosionEvents.EXPLOSION_DETONATE.invoker().onDetonate(level, source, explosion);
	}
}