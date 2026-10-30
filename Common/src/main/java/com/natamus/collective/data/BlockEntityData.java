package com.natamus.collective.data;

import com.natamus.collective.functions.CampfireFunctions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockEntityData {
	public static void addBlockEntityToCache(BlockEntityType<?> blockEntityType) {
		addBlockEntityToCache(blockEntityType, true, true);
	}

	public static void addBlockEntityToCache(BlockEntityType<?> blockEntityType, boolean cacheServerSide, boolean cacheClientSide) {
		if (!GlobalCaches.blockEntitiesToCache.contains(blockEntityType)) {
			GlobalCaches.blockEntitiesToCache.add(blockEntityType);
		}

		if (cacheServerSide && !GlobalCaches.serverSideBlockEntityTypes.contains(blockEntityType)) {
			GlobalCaches.serverSideBlockEntityTypes.add(blockEntityType);
		}

		if (cacheClientSide && !GlobalCaches.clientSideBlockEntityTypes.contains(blockEntityType)) {
			GlobalCaches.clientSideBlockEntityTypes.add(blockEntityType);
		}

		if (!GlobalCaches.cachedBlockEntities.containsKey(blockEntityType)) {
			GlobalCaches.cachedBlockEntities.put(blockEntityType, new WeakHashMap<>());
		}
	}

	public static void addCampfiresToCache() {
		GlobalCaches.cacheCampfires = true;
		addCampfireBlockEntityTypeToCache(BlockEntityType.CAMPFIRE);
	}

	public static void addModdedCampfiresToCache(MinecraftServer server) {
		if (!GlobalCaches.cacheCampfires) {
			return;
		}

		Registry<Block> blockRegistry = server.registryAccess().lookupOrThrow(Registries.BLOCK);
		Registry<BlockEntityType<?>> blockEntityTypeRegistry = server.registryAccess().lookupOrThrow(Registries.BLOCK_ENTITY_TYPE);

		List<BlockState> campfireStates = new ArrayList<>();
		for (Block block : blockRegistry) {
			BlockState defaultState = block.defaultBlockState();
			if (CampfireFunctions.isCampfire(defaultState)) {
				campfireStates.add(defaultState);
			}
		}

		for (BlockEntityType<?> blockEntityType : blockEntityTypeRegistry) {
			for (BlockState campfireState : campfireStates) {
				if (blockEntityType.isValid(campfireState)) {
					addCampfireBlockEntityTypeToCache(blockEntityType);
					break;
				}
			}
		}
	}

	private static void addCampfireBlockEntityTypeToCache(BlockEntityType<?> blockEntityType) {
		addBlockEntityToCache(blockEntityType, true, false);
		if (!GlobalCaches.campfireBlockEntityTypes.contains(blockEntityType)) {
			GlobalCaches.campfireBlockEntityTypes.add(blockEntityType);
		}
	}

	public static List<BlockEntity> getCachedCampfires(Level level) {
		List<BlockEntity> campfires = new ArrayList<>();
		for (BlockEntityType<?> blockEntityType : GlobalCaches.campfireBlockEntityTypes) {
			campfires.addAll(getCachedBlockEntities(blockEntityType, level));
		}

		return campfires;
	}

	public static boolean shouldCacheOnSide(BlockEntityType<?> blockEntityType, boolean isClientSide) {
		if (isClientSide) {
			return GlobalCaches.clientSideBlockEntityTypes.contains(blockEntityType);
		}

		return GlobalCaches.serverSideBlockEntityTypes.contains(blockEntityType);
	}

	public static void cacheBlockEntity(BlockEntityType<?> blockEntityType, Level level, BlockEntity blockEntity) {
		WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>> levelCache = GlobalCaches.cachedBlockEntities.get(blockEntityType);
		if (levelCache == null) {
			return;
		}

		if (!levelCache.containsKey(level)) {
			levelCache.put(level, new CopyOnWriteArrayList<>());
		}

		levelCache.get(level).add(new WeakReference<>(blockEntity));
	}

	public static void uncacheBlockEntity(BlockEntityType<?> blockEntityType, Level level, BlockEntity blockEntity) {
		WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>> levelCache = GlobalCaches.cachedBlockEntities.get(blockEntityType);
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

		WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>> levelCache = GlobalCaches.cachedBlockEntities.get(blockEntityType);
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

		for (WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>> levelCache : GlobalCaches.cachedBlockEntities.values()) {
			levelCache.remove(level);
		}
	}
}
