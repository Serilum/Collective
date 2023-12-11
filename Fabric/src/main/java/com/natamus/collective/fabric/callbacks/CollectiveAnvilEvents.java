package com.natamus.collective.fabric.callbacks;

import oshi.util.tuples.Triplet;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

public class CollectiveAnvilEvents {
	private CollectiveAnvilEvents() { }
	 
    public static final Event<CollectiveAnvilEvents.Anvil_Change> ANVIL_CHANGE = EventFactory.createArrayBacked(CollectiveAnvilEvents.Anvil_Change.class, callbacks -> (anvilmenu, left, right, output, itemName, baseCost, player) -> {
        for (CollectiveAnvilEvents.Anvil_Change callback : callbacks) {
        	Triplet<Integer, Integer, ItemStack> triple = callback.onAnvilChange(anvilmenu, left, right, output, itemName, baseCost, player);
        	if (triple != null) {
        		return triple;
        	}
        }
        
        return null;
    });
    
	@FunctionalInterface
	public interface Anvil_Change {
		 Triplet<Integer, Integer, ItemStack> onAnvilChange(AnvilMenu anvilmenu, ItemStack left, ItemStack right, ItemStack output, String itemName, int baseCost, Player player);
	}
}
