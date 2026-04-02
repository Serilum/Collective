package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;

public final class CollectiveMinecraftServerEvents {
	private CollectiveMinecraftServerEvents() { }
		 
    public static final Event<Set_Spawn> WORLD_SET_SPAWN = EventFactory.createArrayBacked(Set_Spawn.class, callbacks -> (serverLevel, serverLevelData) -> {
        for (Set_Spawn callback : callbacks) {
        	callback.onSetSpawn(serverLevel, serverLevelData);
        }
    });
    
	@FunctionalInterface
	public interface Set_Spawn {
		 void onSetSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData);
	}
}
