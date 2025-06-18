package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveLifecycleEvents;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Language.class, priority = 1001)
public class LanguageMixin {
	@Inject(method = "loadDefault()Lnet/minecraft/locale/Language;", at = @At(value = "TAIL"))
	private static void loadDefault(CallbackInfoReturnable<Language> cir) {
		CollectiveLifecycleEvents.DEFAULT_LANGUAGE_LOADED.invoker().onLanguageLoad();
	}
}
