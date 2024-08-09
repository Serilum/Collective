package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.globalcallbacks.CollectiveGuiCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class, priority = 1001)
public class GuiMixin {
	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "TAIL"))
	public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		CollectiveGuiCallback.ON_GUI_RENDER.invoker().onGuiRender(guiGraphics, deltaTracker);
	}
}
