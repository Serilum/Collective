package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CollectiveAttackEvents {
	private CollectiveAttackEvents() { }
	 
    public static final Event<On_Arrow_Nock> ON_ARROW_NOCK = EventFactory.createArrayBacked(On_Arrow_Nock.class, callbacks -> (item, level, player, hand, hasAmmo) -> {
        for (On_Arrow_Nock callback : callbacks) {
			InteractionResultHolder<ItemStack> resultHolder = callback.onArrowNock(item, level, player, hand, hasAmmo);
        	if (!resultHolder.getResult().equals(InteractionResult.PASS)) {
        		return resultHolder;
        	}
        }
        
        return InteractionResultHolder.pass(item);
    });
    
	@FunctionalInterface
	public interface On_Arrow_Nock {
		 InteractionResultHolder<ItemStack> onArrowNock(ItemStack item, Level level, Player player, InteractionHand hand, boolean hasAmmo);
	}
}
