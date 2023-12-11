package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;

public class CollectiveFurnaceEvents {
	private CollectiveFurnaceEvents() { }
	 
    public static final Event<CollectiveFurnaceEvents.Calculate_Furnace_Burn_Time> CALCULATE_FURNACE_BURN_TIME = EventFactory.createArrayBacked(CollectiveFurnaceEvents.Calculate_Furnace_Burn_Time.class, callbacks -> (itemStack, burntime) -> {
        for (CollectiveFurnaceEvents.Calculate_Furnace_Burn_Time callback : callbacks) {
        	int newburntime = callback.getFurnaceBurnTime(itemStack, burntime);
        	if (burntime != newburntime) {
        		return newburntime;
        	}
        }
        
        return burntime;
    });
    
	@FunctionalInterface
	public interface Calculate_Furnace_Burn_Time {
		 int getFurnaceBurnTime(ItemStack itemStack, int burntime);
	}
}
