package com.natamus.collective.config.screen;

import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.functions.FilterFunctions;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.collective.services.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
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
	public static RegistryAccess worldCreationRegistries = null;
	private static final HashMap<EntityType<?>, SpawnEggItem> spawnEggs = new HashMap<>();

	public static List<String> getIds(String registryName) {
		List<String> ids = getLiveIds(registryName);
		if (ids.isEmpty() && needsWorld(registryName)) {
			ids = DuskConfigRegistryCache.loadIds(registryName);
		}

		Collections.sort(ids);
		return ids;
	}

	public static List<String> getLiveIds(String registryName) {
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
		return ids;
	}

	public static boolean isBuiltInRegistry(String registryName) {
		ResourceLocation registryId = ResourceLocation.tryParse(registryName);
		if (registryId == null) {
			return false;
		}
		return BuiltInRegistries.REGISTRY.containsKey(registryId);
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
		if (identifier != null && registryName.equals("dimension")) {
			return getDimensionIcon(identifier.toString());
		}

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
			if (entityType.getCategory() != MobCategory.MISC) {
				return new ItemStack(Items.EGG);
			}
		}

		if (value instanceof Enchantment) {
			return new ItemStack(Items.ENCHANTED_BOOK);
		}
		if (value instanceof SoundEvent) {
			return new ItemStack(Items.NOTE_BLOCK);
		}
		return ItemStack.EMPTY;
	}

	private static ItemStack getDimensionIcon(String dimensionId) {
		return switch (dimensionId) {
			case "minecraft:overworld" -> new ItemStack(Items.GRASS_BLOCK);
			case "minecraft:the_nether" -> new ItemStack(Items.NETHERRACK);
			case "minecraft:the_end" -> new ItemStack(Items.END_STONE);
			default -> ItemStack.EMPTY;
		};
	}

	public static TextureAtlasSprite getEffectSprite(String registryName, String id) {
		ResourceLocation identifier = ResourceLocation.tryParse(id);
		Registry<?> registry = getRegistry(registryName);
		if (identifier == null || registry == null || !registry.containsKey(identifier)) {
			return null;
		}

		if (registry.get(identifier) instanceof MobEffect mobEffect) {
			return Minecraft.getInstance().getMobEffectTextures().get(mobEffect);
		}
		return null;
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
		if (value instanceof MobEffect mobEffect) {
			return mobEffect.getDisplayName().getString();
		}
		if (value instanceof Enchantment enchantment) {
			return Component.translatable(enchantment.getDescriptionId()).getString();
		}
		if (value instanceof SoundEvent) {
			return getSoundSubtitle(identifier);
		}
		return "";
	}

	public static ItemStack getIconWithVariants(String registryName, String id) {
		String mainEntityId = getMainEntityId(registryName, id);
		if (mainEntityId != null) {
			return getIcon(registryName, mainEntityId);
		}
		return getIcon(registryName, id);
	}

	public static String getNameWithVariants(String registryName, String id) {
		String mainEntityId = getMainEntityId(registryName, id);
		if (mainEntityId != null) {
			return getName(registryName, mainEntityId) + " (" + getVariantName(id, mainEntityId) + ")";
		}
		return getName(registryName, id);
	}

	public static String getMainEntityId(String registryName, String id) {
		ResourceLocation identifier = ResourceLocation.tryParse(id);
		Registry<?> entityRegistry = getRegistry(registryName);
		if (identifier == null || entityRegistry == null || !entityRegistry.key().equals(Registries.ENTITY_TYPE) || entityRegistry.containsKey(identifier)) {
			return null;
		}

		String path = identifier.getPath();
		if (isVillagerProfession(path)) {
			return "minecraft:villager";
		}
		if (path.startsWith("zombie_") && isVillagerProfession(StringFunctions.getTextAfter(path, "zombie_"))) {
			return "minecraft:zombie_villager";
		}

		ResourceLocation mainEntityId = null;
		for (ResourceLocation entityId : entityRegistry.keySet()) {
			boolean isLongerMatch = mainEntityId == null || entityId.getPath().length() > mainEntityId.getPath().length();
			if (entityId.getNamespace().equals(identifier.getNamespace()) && path.endsWith("_" + entityId.getPath()) && isLongerMatch) {
				mainEntityId = entityId;
			}
		}

		if (mainEntityId == null) {
			return null;
		}
		return mainEntityId.toString();
	}

	private static boolean isVillagerProfession(String path) {
		Registry<?> professionRegistry = getRegistry("villager_profession");
		if (professionRegistry == null) {
			return false;
		}

		for (ResourceLocation professionId : professionRegistry.keySet()) {
			if (professionId.getPath().equals(path)) {
				return true;
			}
		}
		return false;
	}

	private static String getVariantName(String id, String mainEntityId) {
		String path = ResourceLocation.tryParse(id).getPath();
		String mainEntityPath = ResourceLocation.tryParse(mainEntityId).getPath();

		String variant = path;
		if (path.endsWith("_" + mainEntityPath)) {
			variant = StringFunctions.getTextBeforeLast(path, "_" + mainEntityPath);
		}
		else if (mainEntityPath.equals("zombie_villager")) {
			variant = StringFunctions.getTextAfter(path, "zombie_");
		}
		return StringFunctions.capitalizeEveryWord(variant.replace("_", " "));
	}

	private static String getSoundSubtitle(ResourceLocation identifier) {
		String subtitleKey = "subtitles." + identifier.getPath();
		if (!Language.getInstance().has(subtitleKey)) {
			return "";
		}
		return Component.translatable(subtitleKey).getString();
	}

	public static Component getAmountLabel(String registryName, int amount) {
		return Component.translatable("options.generic_value", getRegistryLabel(registryName), amount);
	}

	public static Component getSuggestedLabel() {
		return Component.translatable("collective.collective.gui.suggested");
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
			case "mob_effect" -> Component.translatable("collective.collective.gui.effects");
			case "enchantment" -> Component.translatable("collective.collective.gui.enchantments");
			case "sound_event" -> Component.translatable("collective.collective.gui.sounds");
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
			case "flowers" -> Component.translatable("collective.collective.gui.filterflowers");
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

		ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(registryId);
		if (worldCreationRegistries != null) {
			Optional<Registry<Object>> worldCreationRegistry = worldCreationRegistries.registry(registryKey);
			if (worldCreationRegistry.isPresent()) {
				return worldCreationRegistry.get();
			}
		}

		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null) {
			return null;
		}

		Optional<Registry<Object>> worldRegistry = connection.registryAccess().registry(registryKey);
		if (worldRegistry.isPresent()) {
			return worldRegistry.get();
		}
		return getSingleplayerServerRegistry(registryKey);
	}

	private static Registry<?> getSingleplayerServerRegistry(ResourceKey<Registry<Object>> registryKey) {
		IntegratedServer singleplayerServer = Minecraft.getInstance().getSingleplayerServer();
		if (singleplayerServer == null) {
			return null;
		}

		Optional<Registry<Object>> serverRegistry = singleplayerServer.registryAccess().registry(registryKey);
		if (serverRegistry.isPresent()) {
			return serverRegistry.get();
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
