package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.services.Services;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PrimaryLevelData.class, priority = 1001)
public class PrimaryLevelDataMixin {
	@Shadow private boolean confirmedExperimentalWarning;

	@Inject(method = "<init>*", at = @At(value = "TAIL"))
	private void PrimaryLevelData(CallbackInfo ci) {
		if (Services.MODLOADER.isDevelopmentEnvironment() || Services.MODLOADER.isModLoaded("hideexperimentalwarning")) {
			this.confirmedExperimentalWarning = true;
		}
	}

	@Inject(method = "withConfirmedWarning(Z)Lnet/minecraft/world/level/storage/PrimaryLevelData;", at = @At(value = "RETURN"))
	private void withConfirmedWarning(boolean confirmedWarning, CallbackInfoReturnable<PrimaryLevelData> cir) {
		if (Services.MODLOADER.isDevelopmentEnvironment() || Services.MODLOADER.isModLoaded("hideexperimentalwarning")) {
			this.confirmedExperimentalWarning = true;
		}
	}

	@Inject(method = "hasConfirmedExperimentalWarning()Z", at = @At(value = "HEAD"), cancellable = true)
	public void hasConfirmedExperimentalWarning(CallbackInfoReturnable<Boolean> cir) {
		if (Services.MODLOADER.isDevelopmentEnvironment() || Services.MODLOADER.isModLoaded("hideexperimentalwarning")) {
			cir.setReturnValue(true);
		}
	}
}
