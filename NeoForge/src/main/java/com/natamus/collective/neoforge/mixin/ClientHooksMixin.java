package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.services.Services;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientHooks.class, priority = 1001, remap = false)
public class ClientHooksMixin {
	@Inject(method = "createWorldConfirmationScreen(Ljava/lang/Runnable;)V", at = @At(value = "HEAD"), cancellable = true)
	private static void createWorldConfirmationScreen(Runnable doConfirmedWorldLoad, CallbackInfo ci) {
		if (Services.MODLOADER.isDevelopmentEnvironment() || Services.MODLOADER.isModLoaded("hideexperimentalwarning")) {
			doConfirmedWorldLoad.run();
			ci.cancel();
		}
	}
}
