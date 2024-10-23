package com.natamus.collective.data;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockEntityData {
	public static List<BlockEntityType<?>> blockEntitiesToCache = new ArrayList<>();
	public static HashMap<BlockEntityType<?>, WeakHashMap<Level, CopyOnWriteArrayList<BlockEntity>>> cachedBlockEntities = new HashMap<>();

	public static void addBlockEntityToCache(BlockEntityType<?> blockEntityType) {
		if (!blockEntitiesToCache.contains(blockEntityType)) {
			blockEntitiesToCache.add(blockEntityType);
		}

		if (!cachedBlockEntities.containsKey(blockEntityType)) {
			cachedBlockEntities.put(blockEntityType, new WeakHashMap<Level, CopyOnWriteArrayList<BlockEntity>>());
		}
	}
}
