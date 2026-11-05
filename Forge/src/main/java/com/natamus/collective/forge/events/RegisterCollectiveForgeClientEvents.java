package com.natamus.collective.forge.events;

import com.natamus.collective.events.CollectiveClientEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public class RegisterCollectiveForgeClientEvents {
	public static void registerEventsInBus() {
		// BusGroup.DEFAULT.register(MethodHandles.lookup(), RegisterCollectiveForgeClientEvents.class);

		TickEvent.ClientTickEvent.Post.BUS.addListener(RegisterCollectiveForgeClientEvents::onClientTick);
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent.Post e) {
		CollectiveClientEvents.onClientTick();
	}
}
