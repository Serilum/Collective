package com.natamus.collective.data;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalCaches {
	public static List<BlockEntityType<?>> blockEntitiesToCache = new ArrayList<>();
	public static List<BlockEntityType<?>> serverSideBlockEntityTypes = new ArrayList<>();
	public static List<BlockEntityType<?>> clientSideBlockEntityTypes = new ArrayList<>();
	public static HashMap<BlockEntityType<?>, WeakHashMap<Level, CopyOnWriteArrayList<WeakReference<BlockEntity>>>> cachedBlockEntities = new HashMap<>();

	public static HashMap<String, ItemStack> cachedPlayerHeadsMap = new HashMap<>();
	public static HashMap<String, UUID> cachedPlayerNamesMap = new HashMap<>();
	public static HashMap<UUID, String> cachedPlayerUUIDsMap = new HashMap<>();
	public static HashMap<UUID, GameProfile> cachedGameProfileMap = new HashMap<>();

	public static final Map<String, Boolean> bundleConfigCache = new ConcurrentHashMap<>();
	public static final Map<String, Boolean> modEnabledCache = new ConcurrentHashMap<>();

	public static String serverTranslationPackHash = null;
}
