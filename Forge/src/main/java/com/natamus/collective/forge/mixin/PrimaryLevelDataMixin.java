package com.natamus.collective.forge.mixin;

import com.natamus.collective.services.Services;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PrimaryLevelData.class, priority = 1001)
public class PrimaryLevelDataMixin {
	@Shadow private boolean confirmedExperimentalWarning;

	@Inject(method = "<init>*", at = @At(value = "TAIL"))
	private void PrimaryLevelData(CallbackInfo ci) {
		if (Services.MODLOADER.isDevelopmentEnvironment() || Services.MODLOADER.isModLoaded("hideexperimentalwarning")) {
			this.confirmedExperimentalWarning = true;
		}
	}
}
