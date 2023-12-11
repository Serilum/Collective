package com.natamus.collective.mixin;

import com.natamus.collective.globalcallbacks.MainMenuLoadedCallback;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TitleScreen.class, priority = 1001)
public class TitleScreenMixin {
	@Unique private static boolean loadedOnce = false;

	@Inject(method = "init()V", at = @At(value = "TAIL"))
	protected void init(CallbackInfo ci) {
		if (!loadedOnce) {
			MainMenuLoadedCallback.MAIN_MENU_LOADED.invoker().onMainMenuLoaded();
			loadedOnce = true;
		}
	}
}
