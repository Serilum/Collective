package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveSoundEvents;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SoundEngine.class, priority = 1001)
public class SoundEngineMixin {
	@Inject(method = "play", at = @At(value= "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SoundInstance;canPlaySound()Z", ordinal = 0), cancellable = true)
	public void SoundEngine_play(SoundInstance soundInstance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
		SoundEngine soundEngine = (SoundEngine)(Object)this;
		if (!CollectiveSoundEvents.SOUND_PLAY.invoker().onSoundPlay(soundEngine, soundInstance)) {
			cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
		}
	}
}
