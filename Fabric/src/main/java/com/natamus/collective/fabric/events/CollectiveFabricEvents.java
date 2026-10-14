package com.natamus.collective.fabric.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class CollectiveFabricEvents {
	public static WeakHashMap<LivingEntity, List<ItemEntity>> capturedDeathDrops = new WeakHashMap<>();
	public static ArrayDeque<LivingEntity> livingEntitiesDroppingLoot = new ArrayDeque<>();

	public static void onLivingEntityStartsDroppingLoot(LivingEntity livingEntity) {
		livingEntitiesDroppingLoot.push(livingEntity);
		capturedDeathDrops.put(livingEntity, new ArrayList<>());
	}

	public static List<ItemEntity> onLivingEntityStopsDroppingLoot(LivingEntity livingEntity) {
		livingEntitiesDroppingLoot.remove(livingEntity);

		List<ItemEntity> drops = capturedDeathDrops.remove(livingEntity);
		if (drops == null) {
			return new ArrayList<>();
		}

		return drops;
	}

	public static void onEntityAddedToLevel(Entity entity) {
		if (!(entity instanceof ItemEntity itemEntity)) {
			return;
		}

		LivingEntity livingEntity = livingEntitiesDroppingLoot.peek();
		if (livingEntity == null) {
			return;
		}

		capturedDeathDrops.computeIfAbsent(livingEntity, k -> new ArrayList<>()).add(itemEntity);
	}
}
