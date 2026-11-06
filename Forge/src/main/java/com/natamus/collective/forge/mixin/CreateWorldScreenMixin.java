package com.natamus.collective.forge.mixin;

import com.natamus.collective.worldcreation.WorldCreationRows;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreateWorldScreen.class, priority = 1001)
public class CreateWorldScreenMixin {
	@ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/tabs/MenuTabBar$Builder;addTabs([Lnet/minecraft/client/gui/components/tabs/Tab;)Lnet/minecraft/client/gui/components/tabs/MenuTabBar$Builder;"), index = 0)
	private Tab[] init_addTabs(Tab[] tabs) {
		WorldCreationRows.addRowsToTabs((CreateWorldScreen)(Object)this, tabs);
		return tabs;
	}

	@Inject(method = "popScreen", at = @At(value = "HEAD"))
	private void popScreen(CallbackInfo ci) {
		WorldCreationRows.forgetCreateWorldScreen();
	}
}
