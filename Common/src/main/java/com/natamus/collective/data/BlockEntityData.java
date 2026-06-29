package com.natamus.collective.data;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockEntityData {
	public static List<BlockEntityType<?>> blockEntitiesToCache = new ArrayList<>();
	public static List<BlockEntityType<?>> serverSideTypes = new ArrayList<>();
	public static List<BlockEntityType<?>> clientSideTypes = new ArrayList<>();
	public static HashMap<BlockEntityType<?>, WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>>> cachedBlockEntities = new HashMap<>();

	public static void addBlockEntityToCache(BlockEntityType<?> blockEntityType) {
		addBlockEntityToCache(blockEntityType, true, true);
	}

	public static void addBlockEntityToCache(BlockEntityType<?> blockEntityType, boolean cacheServerSide, boolean cacheClientSide) {
		if (!blockEntitiesToCache.contains(blockEntityType)) {
			blockEntitiesToCache.add(blockEntityType);
		}

		if (cacheServerSide && !serverSideTypes.contains(blockEntityType)) {
			serverSideTypes.add(blockEntityType);
		}

		if (cacheClientSide && !clientSideTypes.contains(blockEntityType)) {
			clientSideTypes.add(blockEntityType);
		}

		if (!cachedBlockEntities.containsKey(blockEntityType)) {
			cachedBlockEntities.put(blockEntityType, new WeakHashMap<>());
		}
	}

	public static boolean shouldCacheOnSide(BlockEntityType<?> blockEntityType, boolean isClientSide) {
		if (isClientSide) {
			return clientSideTypes.contains(blockEntityType);
		}

		return serverSideTypes.contains(blockEntityType);
	}

	public static void cacheBlockEntity(BlockEntityType<?> blockEntityType, Level level, BlockEntity blockEntity) {
		WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>> levelCache = cachedBlockEntities.get(blockEntityType);
		if (levelCache == null) {
			return;
		}

		if (!levelCache.containsKey(level)) {
			levelCache.put(level, new CopyOnWriteArrayList<>());
		}

		levelCache.get(level).add(new WeakReference<>(blockEntity));
	}

	public static void uncacheBlockEntity(BlockEntityType<?> blockEntityType, Level level, BlockEntity blockEntity) {
		WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>> levelCache = cachedBlockEntities.get(blockEntityType);
		if (levelCache == null) {
			return;
		}

		CopyOnWriteArrayList<WeakReference<BlockEntity>> blockEntities = levelCache.get(level);
		if (blockEntities == null) {
			return;
		}

		for (WeakReference<BlockEntity> reference : blockEntities) {
			BlockEntity cachedBlockEntity = reference.get();
			if (cachedBlockEntity == null || cachedBlockEntity.equals(blockEntity)) {
				blockEntities.remove(reference);
			}
		}
	}

	public static List<BlockEntity> getCachedBlockEntities(BlockEntityType<?> blockEntityType, Level level) {
		List<BlockEntity> blockEntities = new ArrayList<>();

		WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>> levelCache = cachedBlockEntities.get(blockEntityType);
		if (levelCache == null) {
			return blockEntities;
		}

		CopyOnWriteArrayList<WeakReference<BlockEntity>> cachedReferences = levelCache.get(level);
		if (cachedReferences == null) {
			return blockEntities;
		}

		for (WeakReference<BlockEntity> reference : cachedReferences) {
			BlockEntity cachedBlockEntity = reference.get();
			if (cachedBlockEntity == null) {
				cachedReferences.remove(reference);
				continue;
			}

			blockEntities.add(cachedBlockEntity);
		}

		return blockEntities;
	}

	public static void removeLevelFromCache(Level level) {
		if (level == null) {
			return;
		}

		for (WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>> levelCache : cachedBlockEntities.values()) {
			levelCache.remove(level);
		}
	}
}
