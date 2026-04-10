package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;

public class CollectiveAnimalEvents {
	private CollectiveAnimalEvents() { }
	 
    public static final Event<On_Baby_Spawn> PRE_BABY_SPAWN = EventFactory.createArrayBacked(On_Baby_Spawn.class, callbacks -> (world, parentA, parentB, offspring) -> {
        for (On_Baby_Spawn callback : callbacks) {
        	if (!callback.onBabySpawn(world, parentA, parentB, offspring)) {
        		return false;
        	}
        }
        
        return true;
    });
    
	@FunctionalInterface
	public interface On_Baby_Spawn {
		 boolean onBabySpawn(ServerLevel world, Animal parentA, Animal parentB, AgeableMob offspring);
	}
}
