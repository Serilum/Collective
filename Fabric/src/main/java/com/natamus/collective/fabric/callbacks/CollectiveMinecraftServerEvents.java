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

	public static final Event<Create_Spawn> WORLD_CREATE_SPAWN = EventFactory.createArrayBacked(Create_Spawn.class, callbacks -> (serverLevel, serverLevelData) -> {
		for (Create_Spawn callback : callbacks) {
			if (callback.onCreateSpawn(serverLevel, serverLevelData)) {
				return true;
			}
		}
		return false;
	});

	@FunctionalInterface
	public interface Set_Spawn {
		 void onSetSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData);
	}

	@FunctionalInterface
	public interface Create_Spawn {
		 boolean onCreateSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData);
	}
}
