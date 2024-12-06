package com.natamus.collective.functions;

import com.natamus.collective.events.CollectiveEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.WeakHashMap;

public class SpawnEntityFunctions {
	public static void spawnEntityOnNextTick(ServerLevel serverlevel, Entity entity) {
		CollectiveEvents.entitiesToSpawn.computeIfAbsent(serverlevel, k -> new ArrayList<Entity>()).add(entity);
	}

	public static void startRidingEntityOnNextTick(ServerLevel serverlevel, Entity ridden, Entity rider) {
		CollectiveEvents.entitiesToRide.computeIfAbsent(serverlevel, k -> new WeakHashMap<Entity, Entity>()).put(ridden, rider);
	}
}
