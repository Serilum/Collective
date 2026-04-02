package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;

public class CollectiveClientEvents {
	private CollectiveClientEvents() { }

    public static final Event<Client_World_Load> CLIENT_WORLD_LOAD = EventFactory.createArrayBacked(Client_World_Load.class, callbacks -> (clientLevel) -> {
        for (Client_World_Load callback : callbacks) {
        	callback.onClientWorldLoad(clientLevel);
        }
    });

	@FunctionalInterface
	public interface Client_World_Load {
		 void onClientWorldLoad(ClientLevel clientLevel);
	}
}
