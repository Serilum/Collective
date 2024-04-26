package com.natamus.collective.fabric.mixin;

import com.natamus.collective.services.Services;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreateWorldScreen.class, priority = 1001)
public abstract class CreateWorldScreenMixin {
	@Shadow protected abstract void createNewWorld();

	@Inject(method = "onCreate", at = @At(value = "HEAD"), cancellable = true)
	private void onCreate(CallbackInfo ci) {
		if (Services.MODLOADER.isDevelopmentEnvironment() || Services.MODLOADER.isModLoaded("hideexperimentalwarning")) {
			this.createNewWorld();
			ci.cancel();
		}
	}
}
