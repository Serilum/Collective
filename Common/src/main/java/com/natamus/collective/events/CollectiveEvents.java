package com.natamus.collective.events;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.features.PlayerHeadCacheFeature;
import com.natamus.collective.functions.BlockPosFunctions;
import com.natamus.collective.functions.EntityFunctions;
import com.natamus.collective.functions.SpawnEntityFunctions;
import com.natamus.collective.objects.SAMObject;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectiveEvents {
	public static WeakHashMap<ServerLevel, List<Entity>> entitiesToSpawn = new WeakHashMap<ServerLevel, List<Entity>>();
	public static WeakHashMap<ServerLevel, WeakHashMap<Entity, Entity>> entitiesToRide = new WeakHashMap<ServerLevel, WeakHashMap<Entity, Entity>>();
	public static CopyOnWriteArrayList<Pair<Integer, Runnable>> scheduledRunnables = new CopyOnWriteArrayList<Pair<Integer, Runnable>>();

	public static void onWorldTick(ServerLevel serverLevel) {
		if (entitiesToSpawn.computeIfAbsent(serverLevel, k -> new ArrayList<Entity>()).size() > 0) {
			Entity tospawn = entitiesToSpawn.get(serverLevel).get(0);

			serverLevel.addFreshEntityWithPassengers(tospawn);

			if (entitiesToRide.computeIfAbsent(serverLevel, k -> new WeakHashMap<Entity, Entity>()).containsKey(tospawn)) {
				Entity rider = entitiesToRide.get(serverLevel).get(tospawn);

				rider.startRiding(tospawn);

				entitiesToRide.get(serverLevel).remove(tospawn);
			}

			entitiesToSpawn.get(serverLevel).remove(0);
		}
	}

	public static void onServerTick(MinecraftServer minecraftServer) {
		int serverTickCount = minecraftServer.getTickCount();
		for (Pair<Integer, Runnable> pair : scheduledRunnables) {
			if (pair.getFirst() <= serverTickCount) {
				minecraftServer.tell(new TickTask(minecraftServer.getTickCount(), pair.getSecond()));
				scheduledRunnables.remove(pair);
			}
		}
	}

	public static boolean onEntityJoinLevel(Level level, Entity entity) {
		if (!(entity instanceof LivingEntity)) {
			return true;
		}

		if (entity instanceof Player) {
			Player player = (Player)entity;

			if (RegisterMod.shouldDoCheck) {
				RegisterMod.joinWorldProcess(level, player);
			}

			if (PlayerHeadCacheFeature.isHeadCachingEnabled()) {
				PlayerHeadCacheFeature.cachePlayer(player);
			}
		}

		if (entity.isRemoved()) {
			return true;
		}

		if (GlobalVariables.globalSAMs.isEmpty()) {
			return true;
		}

		Set<String> tags = entity.getTags();
		if (tags.contains(CollectiveReference.MOD_ID + ".checked")) {
			return true;
		}
		entity.addTag(CollectiveReference.MOD_ID + ".checked");

		EntityType<?> entityType = entity.getType();
		if (!GlobalVariables.activeSAMEntityTypes.contains(entityType)) {
			return true;
		}

		boolean isFromSpawner = tags.contains(CollectiveReference.MOD_ID + ".fromspawner");

		List<SAMObject> possibles = new ArrayList<SAMObject>();
		for (SAMObject sam : GlobalVariables.globalSAMs) {
			if (sam == null) {
				continue;
			}

			if (sam.fromEntityType == null) {
				continue;
			}
			if (sam.fromEntityType.equals(entityType)) {
				if ((sam.onlyFromSpawner && !isFromSpawner) || (!sam.onlyFromSpawner && isFromSpawner)) {
					continue;
				}
				possibles.add(sam);
			}
		}

		int size = possibles.size();
		if (size == 0) {
			return true;
		}

		Vec3 eVec = entity.position();
		boolean ageable = entity instanceof AgeableMob;
		boolean isOnSurface = BlockPosFunctions.isOnSurface(level, eVec);

		for (SAMObject sam : possibles) {
			double num = GlobalVariables.random.nextDouble();
			if (num > sam.changeChance) {
				continue;
			}

			if (sam.onlyOnSurface) {
				if (!isOnSurface) {
					continue;
				}
			}
			else if (sam.onlyBelowSurface) {
				if (isOnSurface) {
					continue;
				}
			}

			if (sam.onlyBelowSpecificY) {
				if (eVec.y >= sam.specificY) {
					continue;
				}
			}

			Entity to = sam.toEntityType.create(level);
			if (to == null) {
				return true;
			}

			to.setPos(eVec.x, eVec.y, eVec.z);

			if (ageable && to instanceof AgeableMob) {
				AgeableMob am = (AgeableMob)to;
				am.setAge(((AgeableMob)entity).getAge());
				to = am;
			}

			boolean ignoreMainhand = false;
			if (sam.itemToHold != null) {
				if (to instanceof LivingEntity) {
					LivingEntity le = (LivingEntity)to;
					if (!le.getMainHandItem().getItem().equals(sam.itemToHold)) {
						le.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(sam.itemToHold, 1));
						ignoreMainhand = true;
					}
				}
			}

			boolean ride = false;
			if (EntityFunctions.isHorse(to) && sam.rideNotReplace) {
				AbstractHorse ah = (AbstractHorse)to;
				ah.setTamed(true);

				ride = true;
			}
			else {
				if (CollectiveConfigHandler.transferItemsBetweenReplacedEntities) {
					EntityFunctions.transferItemsBetweenEntities(entity, to, ignoreMainhand);
				}
			}

			if (!(level instanceof ServerLevel)) {
				return true;
			}

			ServerLevel serverLevel = (ServerLevel)level;

			if (ride) {
				SpawnEntityFunctions.startRidingEntityOnNextTick(serverLevel, to, entity);
			}
			else {
				entity.remove(RemovalReason.DISCARDED);
			}

			to.addTag(CollectiveReference.MOD_ID + ".checked");
			SpawnEntityFunctions.spawnEntityOnNextTick(serverLevel, to);

			return ride;
		}

		return true;
	}
}