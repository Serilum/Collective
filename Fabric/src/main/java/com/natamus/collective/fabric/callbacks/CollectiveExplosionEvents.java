package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

public class CollectiveExplosionEvents {
	private CollectiveExplosionEvents() { }
	 
    public static final Event<Explosion_Detonate> EXPLOSION_DETONATE = EventFactory.createArrayBacked(Explosion_Detonate.class, callbacks -> (world, sourceEntity, explosion) -> {
        for (Explosion_Detonate callback : callbacks) {
        	callback.onDetonate(world, sourceEntity, explosion);
        }
    });
    
	@FunctionalInterface
	public interface Explosion_Detonate {
		 void onDetonate(Level world, Entity sourceEntity, Explosion explosion);
	}
}
