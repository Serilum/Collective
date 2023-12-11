package com.natamus.collective.objects;

import com.natamus.collective.data.GlobalVariables;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public class SAMObject {
	public EntityType<?> fromtype;
	public EntityType<?> totype;
	public Item helditem;
	
	public double chance;
	public boolean spawner;
	public boolean rideable;
	public boolean surface;
	
	public SAMObject(EntityType<?> fromentitytype, EntityType<?> toentitytype, Item itemtohold, double changechance, boolean mustbespawner, boolean ridenotreplace, boolean onlyonsurface) {
		fromtype = fromentitytype;
		totype = toentitytype;
		helditem = itemtohold;
		
		chance = changechance;
		spawner = mustbespawner;
		rideable = ridenotreplace;
		surface = onlyonsurface;
		
		GlobalVariables.samobjects.add(this);
		if (!GlobalVariables.activesams.contains(fromtype)) {
			GlobalVariables.activesams.add(fromtype);
		}
	}
}
