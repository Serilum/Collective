package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.data.BlockEntityData;
import com.natamus.collective.services.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 1001)
public class MinecraftMixin {
	@Shadow public ClientLevel level;

	@Inject(method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/gui/screens/ReceivingLevelScreen$Reason;)V", at = @At(value = "HEAD"))
	public void Minecraft_setLevel(ClientLevel clientLevel, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
		BlockEntityData.removeLevelFromCache(level);
	}

	@Inject(method = "createTitle", at = @At("RETURN"), cancellable = true)
	private void Minecraft_createTitle(CallbackInfoReturnable<String> cir) {
        if (CollectiveConfigHandler.updateMinecraftWindowTitleInDevMode && Services.MODLOADER.isDevelopmentEnvironment()) {
            cir.setReturnValue("Minecraft · Dev mode · " + Services.MODLOADER.getModLoaderName());
        }
	}
}
