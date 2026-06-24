package com.natamus.collective.forge.mixin;

import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.services.Services;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 1001)
public class MinecraftMixin {
	@Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
	private void Minecraft_createTitle(CallbackInfoReturnable<String> cir) {
        if (CollectiveConfigHandler.updateMinecraftWindowTitleInDevMode && Services.MODLOADER.isDevelopmentEnvironment()) {
            cir.setReturnValue("Minecraft · Dev mode · " + Services.MODLOADER.getModLoaderName());
        }
	}
}
