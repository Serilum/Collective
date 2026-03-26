package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveEntityEvents;
import net.minecraft.world.entity.monster.MagmaCube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MagmaCube.class, priority = 1001)
public class MagmaCubeMixin {
	@Inject(method = "jumpFromGround", at = @At(value= "TAIL"))
	public void MagmaCube_jumpFromGround(CallbackInfo ci) {
		MagmaCube magmaCube = (MagmaCube)(Object)this;
		CollectiveEntityEvents.ON_ENTITY_IS_JUMPING.invoker().onLivingJump(magmaCube.level(), magmaCube);
	}
}
