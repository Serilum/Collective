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

	public SAMObject(EntityType<?> fromEntityTypeIn, EntityType<?> toEntityTypeIn, Item itemToHoldIn, double changeChanceIn, boolean onlyFromSpawnerIn, boolean rideNotReplaceIn, boolean onlyOnSurfaceIn) {
		new SAMObject(fromEntityTypeIn, toEntityTypeIn, itemToHoldIn, changeChanceIn, onlyFromSpawnerIn, rideNotReplaceIn, onlyOnSurfaceIn, false);
	}
	public SAMObject(EntityType<?> fromEntityTypeIn, EntityType<?> toEntityTypeIn, Item itemToHoldIn, double changeChanceIn, boolean onlyFromSpawnerIn, boolean rideNotReplaceIn, boolean onlyOnSurfaceIn, boolean onlyBelowSurfaceIn) {
		fromEntityType = fromEntityTypeIn;
		toEntityType = toEntityTypeIn;
		itemToHold = itemToHoldIn;
		
		changeChance = changeChanceIn;
		onlyFromSpawner = onlyFromSpawnerIn;
		rideNotReplace = rideNotReplaceIn;
		onlyOnSurface = onlyOnSurfaceIn;
		onlyBelowSurface = onlyBelowSurfaceIn;
		
		GlobalVariables.globalSAMs.add(this);
		if (!GlobalVariables.activeSAMEntityTypes.contains(fromEntityType)) {
			GlobalVariables.activeSAMEntityTypes.add(fromEntityType);
		}
	}
}
