package com.natamus.collective;

import com.natamus.collective.fabric.networking.FabricNetworkHandler;
import com.natamus.collective.implementations.networking.NetworkSetup;
import com.natamus.collective.implementations.networking.data.Side;
import net.fabricmc.api.ClientModInitializer;

public class CollectiveFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		new NetworkSetup(new FabricNetworkHandler(Side.CLIENT));

		registerEvents();
	}
	
	private void registerEvents() {

	}
}
