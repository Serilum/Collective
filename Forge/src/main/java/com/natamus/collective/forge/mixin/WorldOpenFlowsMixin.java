package com.natamus.collective.forge.mixin;

import com.mojang.serialization.Lifecycle;
import com.natamus.collective.services.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldOpenFlows.class, priority = 1001)
public class WorldOpenFlowsMixin {
	@ModifyVariable(method = "doLoadLevel*", at = @At(value= "STORE"), ordinal = 5)
	public boolean loadLevel_bl2(boolean isLifeCycleStable) {
		return !Services.MODLOADER.isDevelopmentEnvironment() && !Services.MODLOADER.isModLoaded("hideexperimentalwarning");
	}

	@Inject(method = "confirmWorldCreation", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/serialization/Lifecycle;experimental()Lcom/mojang/serialization/Lifecycle;"), cancellable = true)
	private static void confirmWorldCreation(Minecraft minecraft, CreateWorldScreen screen, Lifecycle lifecycle, Runnable loadWorld, boolean skipWarnings, CallbackInfo ci) {
		if (Services.MODLOADER.isDevelopmentEnvironment() || Services.MODLOADER.isModLoaded("hideexperimentalwarning")) {
			loadWorld.run();
			ci.cancel();
		}
	}
}
