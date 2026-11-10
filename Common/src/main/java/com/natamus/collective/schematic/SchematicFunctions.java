package com.natamus.collective.schematic;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

public class SchematicFunctions {
	public static Vec3 getPosition(ListTag doubleList) {
		return new Vec3(doubleList.getDouble(0), doubleList.getDouble(1), doubleList.getDouble(2));
	}

	public static ListTag getDoubleList(Vec3 position) {
		ListTag doubleList = new ListTag();
		doubleList.add(DoubleTag.valueOf(position.x));
		doubleList.add(DoubleTag.valueOf(position.y));
		doubleList.add(DoubleTag.valueOf(position.z));
		return doubleList;
	}
}
