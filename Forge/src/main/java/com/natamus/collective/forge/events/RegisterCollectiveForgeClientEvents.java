package com.natamus.collective.forge.events;

import com.natamus.collective.events.CollectiveClientEvents;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RegisterCollectiveForgeClientEvents {
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (!e.phase.equals(TickEvent.Phase.END)) {
            return;
        }

        CollectiveClientEvents.onClientTick();
    }
}