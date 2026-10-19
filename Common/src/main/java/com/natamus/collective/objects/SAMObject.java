package com.natamus.collective.objects;

import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.functions.ConfigFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.Locale;
import java.util.Optional;

public class SAMObject {
	public EntityType<?> fromEntityType;
	public EntityType<?> toEntityType;
	public Item itemToHold;

	public double changeChance;
	public boolean onlyFromSpawner;
	public boolean rideNotReplace;
	public boolean onlyOnSurface;
	public boolean onlyBelowSurface;
	public boolean onlyBelowSpecificY;
	public int specificY;

	public String dimensionIds = "";
	public String biomeIds = "";

	public SAMObject(EntityType<?> fromEntityTypeIn, EntityType<?> toEntityTypeIn, Item itemToHoldIn, double changeChanceIn, boolean onlyFromSpawnerIn, boolean rideNotReplaceIn, boolean onlyOnSurfaceIn) {
		new SAMObject(fromEntityTypeIn, toEntityTypeIn, itemToHoldIn, changeChanceIn, onlyFromSpawnerIn, rideNotReplaceIn, onlyOnSurfaceIn, false);
	}
	public SAMObject(EntityType<?> fromEntityTypeIn, EntityType<?> toEntityTypeIn, Item itemToHoldIn, double changeChanceIn, boolean onlyFromSpawnerIn, boolean rideNotReplaceIn, boolean onlyOnSurfaceIn, boolean onlyBelowSurfaceIn) {
		new SAMObject(fromEntityTypeIn, toEntityTypeIn, itemToHoldIn, changeChanceIn, onlyFromSpawnerIn, rideNotReplaceIn, onlyOnSurfaceIn, onlyBelowSurfaceIn, false, Integer.MAX_VALUE);
	}
	public SAMObject(EntityType<?> fromEntityTypeIn, EntityType<?> toEntityTypeIn, Item itemToHoldIn, double changeChanceIn, boolean onlyFromSpawnerIn, boolean rideNotReplaceIn, boolean onlyOnSurfaceIn, boolean onlyBelowSurfaceIn, boolean onlyBelowSpecificYIn, int specificYIn) {
		fromEntityType = fromEntityTypeIn;
		toEntityType = toEntityTypeIn;
		itemToHold = itemToHoldIn;

		changeChance = changeChanceIn;
		onlyFromSpawner = onlyFromSpawnerIn;
		rideNotReplace = rideNotReplaceIn;
		onlyOnSurface = onlyOnSurfaceIn;
		onlyBelowSurface = onlyBelowSurfaceIn;
		onlyBelowSpecificY = onlyBelowSpecificYIn;
		specificY = specificYIn;

		GlobalVariables.globalSAMs.add(this);
		if (!GlobalVariables.activeSAMEntityTypes.contains(fromEntityType)) {
			GlobalVariables.activeSAMEntityTypes.add(fromEntityType);
		}
	}

	public boolean isInAllowedDimension(Level level) {
		if (dimensionIds.isBlank()) {
			return true;
		}
		return ConfigFunctions.isInIdList(dimensionIds, level.dimension().identifier().toString());
	}

	public boolean isInAllowedBiome(Level level, BlockPos pos) {
		if (biomeIds.isBlank()) {
			return true;
		}

		Holder<Biome> biome = level.getBiome(pos);
		for (String rawEntry : biomeIds.split(",")) {
			String entry = rawEntry.strip().toLowerCase(Locale.ROOT);
			if (!entry.startsWith("#")) {
				continue;
			}

			Identifier tagIdentifier = Identifier.tryParse(entry.substring(1));
			if (tagIdentifier != null && biome.is(TagKey.create(Registries.BIOME, tagIdentifier))) {
				return true;
			}
		}

		Optional<ResourceKey<Biome>> biomeKey = biome.unwrapKey();
		if (!biomeKey.isPresent()) {
			return false;
		}
		return ConfigFunctions.isInIdList(biomeIds, biomeKey.get().identifier().toString());
	}
}
