package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveEntityEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = LightningBolt.class, priority = 1001)
public class LightningBoltMixin {
	@SuppressWarnings("InvalidInjectorMethodSignature")
	@Inject(method = "tick()V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void tick(CallbackInfo ci, List<Entity> list) {
		LightningBolt lightningBolt = (LightningBolt)(Object)this;
		list.removeIf(entity -> !CollectiveEntityEvents.ON_ENTITY_LIGHTNING_STRIKE.invoker().onLightningStrike(entity.level(), entity, lightningBolt));
	}
}