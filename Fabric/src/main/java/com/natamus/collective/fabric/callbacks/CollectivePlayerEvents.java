package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class CollectivePlayerEvents {
	private CollectivePlayerEvents() { }

    public static final Event<Player_Tick> PLAYER_TICK = EventFactory.createArrayBacked(Player_Tick.class, callbacks -> (world, player) -> {
        for (Player_Tick callback : callbacks) {
        	callback.onTick(world, player);
        }
    });
	
    public static final Event<Player_Death> PLAYER_DEATH = EventFactory.createArrayBacked(Player_Death.class, callbacks -> (world, player) -> {
        for (Player_Death callback : callbacks) {
        	callback.onDeath(world, player);
        }
    });
    
    public static final Event<Player_Change_Dimension> PLAYER_CHANGE_DIMENSION = EventFactory.createArrayBacked(Player_Change_Dimension.class, callbacks -> (world, player) -> {
        for (Player_Change_Dimension callback : callbacks) {
        	callback.onChangeDimension(world, player);
        }
    });
    
    public static final Event<Player_Dig_Speed_Calc> ON_PLAYER_DIG_SPEED_CALC = EventFactory.createArrayBacked(Player_Dig_Speed_Calc.class, callbacks -> (world, player, digSpeed, state) -> {
        for (Player_Dig_Speed_Calc callback : callbacks) {
        	float newSpeed = callback.onDigSpeedCalc(world, player, digSpeed, state);
        	if (newSpeed != digSpeed) {
        		return newSpeed;
        	}
        }
        
        return -1;
    });

    public static final Event<Player_Picked_Up_Item> ON_ITEM_PICKED_UP = EventFactory.createArrayBacked(Player_Picked_Up_Item.class, callbacks -> (level, player, itemStack) -> {
        for (Player_Picked_Up_Item callback : callbacks) {
        	callback.onItemPickedUp(level, player, itemStack);
        }
    });
    
    public static final Event<Player_Logged_In> PLAYER_LOGGED_IN = EventFactory.createArrayBacked(Player_Logged_In.class, callbacks -> (world, player) -> {
        for (Player_Logged_In callback : callbacks) {
        	callback.onPlayerLoggedIn(world, player);
        }
    });
    
    public static final Event<Player_Logged_Out> PLAYER_LOGGED_OUT = EventFactory.createArrayBacked(Player_Logged_Out.class, callbacks -> (world, player) -> {
        for (Player_Logged_Out callback : callbacks) {
        	callback.onPlayerLoggedOut(world, player);
        }
    });
    
	@FunctionalInterface
	public interface Player_Tick {
		 void onTick(ServerLevel world, ServerPlayer player);
	}
    
	@FunctionalInterface
	public interface Player_Death {
		 void onDeath(ServerLevel world, ServerPlayer player);
	}
	
	@FunctionalInterface
	public interface Player_Change_Dimension {
		 void onChangeDimension(ServerLevel world, ServerPlayer player);
	}
	
	@FunctionalInterface
	public interface Player_Dig_Speed_Calc {
		 float onDigSpeedCalc(Level world, Player player, float digSpeed, BlockState state);
	}

	@FunctionalInterface
	public interface Player_Picked_Up_Item {
		 void onItemPickedUp(Level level, Player player, @Nullable ItemStack itemStack);
	}
	
	@FunctionalInterface
	public interface Player_Logged_In {
		 void onPlayerLoggedIn(Level world, Player player);
	}
	
	@FunctionalInterface
	public interface Player_Logged_Out {
		 void onPlayerLoggedOut(Level world, Player player);
	}
}
