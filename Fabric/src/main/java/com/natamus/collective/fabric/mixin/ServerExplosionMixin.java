package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveExplosionEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerExplosion.class, priority = 1001)
public class ServerExplosionMixin {
	@Final @Shadow private Entity source;
	
	@Inject(method = "explode()V", at = @At(value = "HEAD"))
	public void Explosion_explode(CallbackInfo ci) {
		Explosion explosion = (Explosion)(Object)this;
		CollectiveExplosionEvents.EXPLOSION_DETONATE.invoker().onDetonate(explosion.level(), source, explosion);
	}
}