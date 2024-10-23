package com.natamus.collective.globalcallbacks;

import com.natamus.collective.implementations.event.Event;
import com.natamus.collective.implementations.event.EventFactory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CachedBlockEntityCallback {
	private CachedBlockEntityCallback() { }

    public static final Event<On_Block_Entity_Added> BLOCK_ENTITY_ADDED = EventFactory.createArrayBacked(On_Block_Entity_Added.class, callbacks -> (level, blockEntity, blockEntityType) -> {
        for (On_Block_Entity_Added callback : callbacks) {
        	callback.onBlockEntityAdded(level, blockEntity, blockEntityType);
        }
    });

    public static final Event<On_Block_Entity_Removed> BLOCK_ENTITY_REMOVED = EventFactory.createArrayBacked(On_Block_Entity_Removed.class, callbacks -> (level, blockEntity, blockEntityType) -> {
        for (On_Block_Entity_Removed callback : callbacks) {
        	callback.onBlockEntityRemoved(level, blockEntity, blockEntityType);
        }
    });

	@FunctionalInterface
	public interface On_Block_Entity_Added {
		 void onBlockEntityAdded(Level level, BlockEntity blockEntity, BlockEntityType<?> blockEntityType);
	}

	@FunctionalInterface
	public interface On_Block_Entity_Removed {
		 void onBlockEntityRemoved(Level level, BlockEntity blockEntity, BlockEntityType<?> blockEntityType);
	}
}
