package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;

public class CollectiveWorldEvents {
	private CollectiveWorldEvents() { }
	 
    public static final Event<World_Unload> WORLD_UNLOAD = EventFactory.createArrayBacked(World_Unload.class, callbacks -> (serverLevel) -> {
        for (World_Unload callback : callbacks) {
        	callback.onWorldUnload(serverLevel);
        }
    });
    
	@FunctionalInterface
	public interface World_Unload {
		 void onWorldUnload(ServerLevel serverLevel);
	}
}
