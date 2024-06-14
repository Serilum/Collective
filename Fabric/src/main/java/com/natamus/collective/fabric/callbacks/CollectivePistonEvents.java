package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class CollectivePistonEvents {
	private CollectivePistonEvents() { }
	 
    public static final Event<Piston_Activate> PRE_PISTON_ACTIVATE = EventFactory.createArrayBacked(Piston_Activate.class, callbacks -> (level, blockPos, direction, isExtending) -> {
        for (Piston_Activate callback : callbacks) {
        	if (!callback.onPistonActivate(level, blockPos, direction, isExtending)) {
				return false;
			}
        }
		return true;
    });
    
	@FunctionalInterface
	public interface Piston_Activate {
		 boolean onPistonActivate(Level level, BlockPos blockPos, Direction direction, boolean isExtending);
	}
}
