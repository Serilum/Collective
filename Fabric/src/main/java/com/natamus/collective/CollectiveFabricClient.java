package com.natamus.collective;

import com.natamus.collective.events.CollectiveClientEvents;
import com.natamus.collective.fabric.networking.FabricNetworkHandler;
import com.natamus.collective.implementations.networking.NetworkSetup;
import com.natamus.collective.implementations.networking.data.Side;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class CollectiveFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		new NetworkSetup(new FabricNetworkHandler(Side.CLIENT));

		CollectiveCommon.registerPackets();

		registerEvents();
	}
	
	private void registerEvents() {
		ClientTickEvents.END_CLIENT_TICK.register((Minecraft mc) -> {
			CollectiveClientEvents.onClientTick();
		});
	}
}
