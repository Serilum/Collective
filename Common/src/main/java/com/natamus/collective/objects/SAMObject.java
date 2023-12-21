package com.natamus.collective.objects;

import com.natamus.collective.data.GlobalVariables;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

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
}
