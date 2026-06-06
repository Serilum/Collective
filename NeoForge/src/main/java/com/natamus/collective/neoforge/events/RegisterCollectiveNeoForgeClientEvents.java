package com.natamus.collective.neoforge.events;

import com.natamus.collective.events.CollectiveClientEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class RegisterCollectiveNeoForgeClientEvents {
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post e) {
        CollectiveClientEvents.onClientTick();
	}
}
