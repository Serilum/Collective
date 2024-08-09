package com.natamus.collective.fabric.mixin;

import com.mojang.serialization.Lifecycle;
import com.natamus.collective.services.Services;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SuppressWarnings({"deprecation", "rawtypes", "unchecked", })
@Mixin(value = CreateWorldScreen.class, priority = 1001)
public abstract class CreateWorldScreenMixin {
	@Shadow
	private boolean recreated;
	@SuppressWarnings("deprecation")
	@Shadow
	protected abstract void createNewWorld(PrimaryLevelData.SpecialWorldProperty specialWorldProperty, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, Lifecycle worldGenSettingsLifecycle);

	@Inject(method = "onCreate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;confirmWorldCreation(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private void onCreate(CallbackInfo ci, WorldCreationContext worldCreationContext, WorldDimensions.Complete complete, LayeredRegistryAccess layeredRegistryAccess, Lifecycle lifecycle, Lifecycle lifecycle2, Lifecycle lifecycle3, boolean bl) {
		if (this.recreated) {
			return;
		}

		if (Services.MODLOADER.isDevelopmentEnvironment() || Services.MODLOADER.isModLoaded("hideexperimentalwarning")) {
			this.createNewWorld(complete.specialWorldProperty(), layeredRegistryAccess, lifecycle3);
			ci.cancel();
		}
	}
}
