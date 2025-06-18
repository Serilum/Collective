package com.natamus.collective.fabric.callbacks;

import com.natamus.collective.implementations.event.Event;
import com.natamus.collective.implementations.event.EventFactory;
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
