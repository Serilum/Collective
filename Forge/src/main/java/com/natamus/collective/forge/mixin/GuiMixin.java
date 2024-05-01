package com.natamus.collective.forge.mixin;

import com.natamus.collective.globalcallbacks.CollectiveGuiCallback;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class, priority = 1001)
public class GuiMixin {
	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(value = "TAIL"))
	public void render(GuiGraphics guiGraphics, float tickDelta, CallbackInfo ci) {
		CollectiveGuiCallback.ON_GUI_RENDER.invoker().onGuiRender(guiGraphics, tickDelta);
	}
}
