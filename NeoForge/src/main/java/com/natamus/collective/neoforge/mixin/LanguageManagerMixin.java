package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.translations.TranslationLoader;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LanguageManager.class, priority = 1001)
public abstract class LanguageManagerMixin {
	@Shadow public abstract String getSelected();

	@Inject(method = "onResourceManagerReload", at = @At("TAIL"))
	private void collective$onLanguageReload(ResourceManager resourceManager, CallbackInfo ci) {
		TranslationLoader.onLanguageChanged(this.getSelected());
	}
}
