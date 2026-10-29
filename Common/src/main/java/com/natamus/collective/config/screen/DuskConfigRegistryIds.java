package com.natamus.collective.config.screen;

import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.functions.FilterFunctions;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.collective.services.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class DuskConfigRegistryIds {
	private static final HashMap<EntityType<?>, SpawnEggItem> spawnEggs = new HashMap<>();

	public static List<String> getIds(String registryName) {
		List<String> ids = new ArrayList<>();
		if (registryName.equals("dimension")) {
			addDimensionIds(ids);
		}
		else if (registryName.equals(DuskConfigModToggles.modRegistry)) {
			addModIds(ids);
		}
		else {
			Registry<?> registry = getRegistry(registryName);
			if (registry != null) {
				for (ResourceLocation identifier : registry.keySet()) {
					ids.add(identifier.toString());
				}
			}
		}

		Collections.sort(ids);
		return ids;
	}

	public static List<String> getSuggestedIds(String registryName, String filter) {
		List<String> ids = new ArrayList<>();

		Registry<?> registry = getRegistry(registryName);
		if (filter.isEmpty() || registry == null) {
			return ids;
		}

		try {
			for (ResourceLocation identifier : registry.keySet()) {
				if (FilterFunctions.isInFilter(filter, registry.get(identifier))) {
					ids.add(identifier.toString());
				}
			}
		}
		catch (Exception ignored) {
			ids.clear();
		}

		Collections.sort(ids);
		return ids;
	}

	public static ItemStack getIcon(String registryName, String id) {
		if (registryName.equals(DuskConfigModToggles.modRegistry)) {
			return ItemStack.EMPTY;
		}

		ResourceLocation identifier = ResourceLocation.tryParse(id);
		Registry<?> registry = getRegistry(registryName);
		if (identifier == null || registry == null || !registry.containsKey(identifier)) {
			return ItemStack.EMPTY;
		}

		Object value = registry.get(identifier);
		if (value instanceof ItemLike itemLike) {
			return new ItemStack(itemLike);
		}

		if (value instanceof EntityType<?> entityType) {
			SpawnEggItem spawnEgg = getSpawnEgg(entityType);
			if (spawnEgg != null) {
				return new ItemStack(spawnEgg);
			}
		}
		return ItemStack.EMPTY;
	}

	private static SpawnEggItem getSpawnEgg(EntityType<?> entityType) {
		Registry<?> itemRegistry = getRegistry("item");
		if (spawnEggs.isEmpty() && itemRegistry != null) {
			for (Object item : itemRegistry) {
				if (item instanceof SpawnEggItem spawnEggItem) {
					spawnEggs.put(spawnEggItem.getType(null), spawnEggItem);
				}
			}
		}
		return spawnEggs.get(entityType);
	}

	public static String getName(String registryName, String id) {
		if (registryName.equals(DuskConfigModToggles.modRegistry)) {
			return Services.MODLOADER.getModName(id);
		}

		ResourceLocation identifier = ResourceLocation.tryParse(id);
		Registry<?> registry = getRegistry(registryName);
		if (identifier == null || registry == null || !registry.containsKey(identifier)) {
			return "";
		}

		Object value = registry.get(identifier);
		if (value instanceof Item item) {
			return Component.translatable(item.getDescriptionId()).getString();
		}
		if (value instanceof Block block) {
			return block.getName().getString();
		}
		if (value instanceof EntityType<?> entityType) {
			return entityType.getDescription().getString();
		}
		if (value instanceof Biome) {
			return Component.translatable(identifier.toLanguageKey("biome")).getString();
		}
		return "";
	}

	public static Component getAmountLabel(String registryName, int amount) {
		return Component.translatable("options.generic_value", getRegistryLabel(registryName), amount);
	}

	public static Component getAllLabel(String registryName) {
		return Component.translatable("options.generic_value", getRegistryLabel(registryName), Component.translatable("gui.all"));
	}

	private static Component getRegistryLabel(String registryName) {
		String registryPath = registryName.substring(registryName.indexOf(':') + 1);
		return switch (registryPath) {
			case "item" -> Component.translatable("stat.itemsButton");
			case "block" -> Component.translatable("collective.collective.gui.blocks");
			case "entity_type" -> Component.translatable("collective.collective.gui.entities");
			case "dimension" -> Component.translatable("collective.collective.gui.dimensions");
			case "worldgen/biome" -> Component.translatable("collective.collective.gui.biomes");
			case DuskConfigModToggles.modRegistry -> Component.translatable("collective.collective.gui.mods");
			default -> Component.translatable("collective.collective.gui.entries");
		};
	}

	public static boolean needsWorld(String registryName) {
		return !registryName.equals(DuskConfigModToggles.modRegistry);
	}

	public static Component getFilterLabel(String filter) {
		return switch (filter) {
			case "tools" -> Component.translatable("collective.collective.gui.filtertools");
			case "axes" -> Component.translatable("collective.collective.gui.filteraxes");
			case "pickaxes" -> Component.translatable("collective.collective.gui.filterpickaxes");
			case "shovels" -> Component.translatable("collective.collective.gui.filtershovels");
			case "hoes" -> Component.translatable("collective.collective.gui.filterhoes");
			case "swords" -> Component.translatable("collective.collective.gui.filterswords");
			case "blocks" -> Component.translatable("collective.collective.gui.blocks");
			case "logs" -> Component.translatable("collective.collective.gui.filterlogs");
			case "leaves" -> Component.translatable("collective.collective.gui.filterleaves");
			case "ores" -> Component.translatable("collective.collective.gui.filterores");
			case "mobs" -> Component.translatable("collective.collective.gui.filtermobs");
			case "monsters" -> Component.translatable("collective.collective.gui.filtermonsters");
			case "animals" -> Component.translatable("collective.collective.gui.filteranimals");
			default -> Component.literal(StringFunctions.capitalizeFirst(filter));
		};
	}

	private static Registry<?> getRegistry(String registryName) {
		ResourceLocation registryId = ResourceLocation.tryParse(registryName);
		if (registryId == null) {
			return null;
		}

		Registry<?> builtInRegistry = BuiltInRegistries.REGISTRY.get(registryId);
		if (builtInRegistry != null) {
			return builtInRegistry;
		}

		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return null;
		}

		Optional<Registry<Object>> worldRegistry = connection.registryAccess().registry(ResourceKey.createRegistryKey(registryId));
		if (worldRegistry.isPresent()) {
			return worldRegistry.get();
		}
		return null;
	}

	private static void addModIds(List<String> ids) {
		ids.addAll(DuskConfigModToggles.getListableModIds());
	}

	private static void addDimensionIds(List<String> ids) {
		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return;
		}

		for (ResourceKey<Level> levelKey : connection.levels()) {
			ids.add(levelKey.location().toString());
		}
	}
}
