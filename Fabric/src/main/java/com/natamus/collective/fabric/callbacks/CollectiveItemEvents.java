package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CollectiveItemEvents {
	private CollectiveItemEvents() { }
	 
    public static final Event<Item_Expire> ON_ITEM_EXPIRE = EventFactory.createArrayBacked(Item_Expire.class, callbacks -> (itemEntity, itemStack) -> {
        for (Item_Expire callback : callbacks) {
        	callback.onItemExpire(itemEntity, itemStack);
        }
    });
    
    public static final Event<Item_Fished> ON_ITEM_FISHED = EventFactory.createArrayBacked(Item_Fished.class, callbacks -> (loot, hook) -> {
        for (Item_Fished callback : callbacks) {
        	callback.onItemFished(loot, hook);
        }
    });
    
    public static final Event<Item_Tossed> ON_ITEM_TOSSED = EventFactory.createArrayBacked(Item_Tossed.class, callbacks -> (player, itemStack) -> {
        for (Item_Tossed callback : callbacks) {
        	callback.onItemTossed(player, itemStack);
        }
    });

	public static final Event<Item_Destroyed> ON_ITEM_DESTROYED = EventFactory.createArrayBacked(Item_Destroyed.class, callbacks -> (player, itemStack, hand) -> {
		for (Item_Destroyed callback : callbacks) {
			callback.onItemDestroyed(player, itemStack, hand);
		}
	});

	public static final Event<Item_Use_Finished> ON_ITEM_USE_FINISHED = EventFactory.createArrayBacked(Item_Use_Finished.class, callbacks -> (player, usedItem, newItem, hand) -> {
		for (Item_Use_Finished callback : callbacks) {
			ItemStack changedStack = callback.onItemUsedFinished(player, usedItem, newItem, hand);
			if (changedStack != null) {
				return changedStack;
			}
		}
		return null;
	});
    
	@FunctionalInterface
	public interface Item_Expire {
		 void onItemExpire(ItemEntity itemEntity, ItemStack itemStack);
	}
	
	@FunctionalInterface
	public interface Item_Fished {
		 void onItemFished(List<ItemStack> loot, FishingHook hook);
	}
	
	@FunctionalInterface
	public interface Item_Tossed {
		 void onItemTossed(Player player, ItemStack itemStack);
	}

	@FunctionalInterface
	public interface Item_Destroyed {
		void onItemDestroyed(Player player, ItemStack stack, InteractionHand hand);
	}

	@FunctionalInterface
	public interface Item_Use_Finished {
		ItemStack onItemUsedFinished(Player player, ItemStack usedItem, ItemStack newItem, InteractionHand hand);
	}
}
