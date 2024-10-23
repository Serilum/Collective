package com.natamus.collective;

import com.natamus.collective.fabric.networking.FabricNetworkHandler;
import com.natamus.collective.globalcallbacks.CollectiveGuiCallback;
import com.natamus.collective.implementations.networking.NetworkSetup;
import com.natamus.collective.implementations.networking.data.Side;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class CollectiveFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		new NetworkSetup(new FabricNetworkHandler(Side.CLIENT));

		registerEvents();
	}
	
	private void registerEvents() {
		HudRenderCallback.EVENT.register((GuiGraphics guiGraphics, DeltaTracker deltaTracker) -> {
			CollectiveGuiCallback.ON_GUI_RENDER.invoker().onGuiRender(guiGraphics, deltaTracker);
		});
	}
}
