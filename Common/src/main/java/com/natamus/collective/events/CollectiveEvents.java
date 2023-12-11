package com.natamus.collective.events;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.data.GlobalVariables;
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

	public static void onWorldTick(ServerLevel serverlevel) {
		if (entitiesToSpawn.computeIfAbsent(serverlevel, k -> new ArrayList<Entity>()).size() > 0) {
			Entity tospawn = entitiesToSpawn.get(serverlevel).get(0);

			serverlevel.addFreshEntityWithPassengers(tospawn);

			if (entitiesToRide.computeIfAbsent(serverlevel, k -> new WeakHashMap<Entity, Entity>()).containsKey(tospawn)) {
				Entity rider = entitiesToRide.get(serverlevel).get(tospawn);

				rider.startRiding(tospawn);

				entitiesToRide.get(serverlevel).remove(tospawn);
			}

			entitiesToSpawn.get(serverlevel).remove(0);
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
	
	public static boolean onEntityJoinLevel(Level world, Entity entity) {
		if (!(entity instanceof LivingEntity)) {
			return true;
		}
		
		if (RegisterMod.shouldDoCheck) {
			if (entity instanceof Player) {
				RegisterMod.joinWorldProcess(world, (Player)entity);
			}
		}
		
		if (entity.isRemoved()) {
			return true;
		}
		
		if (GlobalVariables.samobjects.isEmpty()) {
			return true;
		}
		
		Set<String> tags = entity.getTags();
		if (tags.contains(CollectiveReference.MOD_ID + ".checked")) {
			return true;
		}
		entity.addTag(CollectiveReference.MOD_ID + ".checked");

		EntityType<?> entitytype = entity.getType();
		if (entitytype == null || !GlobalVariables.activesams.contains(entitytype)) {
			return true;
		}

		boolean isspawner = tags.contains(CollectiveReference.MOD_ID + ".fromspawner");
		
		List<SAMObject> possibles = new ArrayList<SAMObject>();
		for (SAMObject samobject : GlobalVariables.samobjects) {
			if (samobject == null) {
				continue;
			}

			if (samobject.fromtype == null) {
				continue;
			}
			if (samobject.fromtype.equals(entitytype)) {
				if ((samobject.spawner && !isspawner) || (!samobject.spawner && isspawner)) {
					continue;
				}
				possibles.add(samobject);
			}
		}
		
		int size = possibles.size();
		if (size == 0) {
			return true;
		}

		boolean ageable = entity instanceof AgeableMob;

		for (SAMObject sam : possibles) {
			double num = GlobalVariables.random.nextDouble();
			if (num > sam.chance) {
				continue;
			}
			
			Vec3 evec = entity.position();
			if (sam.surface) {
				if (!BlockPosFunctions.isOnSurface(world, evec)) {
					continue;
				}
			}
			
			Entity to = sam.totype.create(world);
			if (to == null) {
				return true;
			}

			//to.setWorld(Level);
			to.setPos(evec.x, evec.y, evec.z);

			if (ageable && to instanceof AgeableMob) {
				AgeableMob am = (AgeableMob)to;
				am.setAge(((AgeableMob)entity).getAge());
				to = am;
			}

			boolean ignoremainhand = false;
			if (sam.helditem != null) {
				if (to instanceof LivingEntity) {
					LivingEntity le = (LivingEntity)to;
					if (!le.getMainHandItem().getItem().equals(sam.helditem)) {
						le.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(sam.helditem, 1));
						ignoremainhand = true;
					}
				}
			}
			
			boolean ride = false;
			if (EntityFunctions.isHorse(to) && sam.rideable) {
				AbstractHorse ah = (AbstractHorse)to;
				ah.setTamed(true);

				ride = true;
			}
			else {
				if (CollectiveConfigHandler.transferItemsBetweenReplacedEntities) {
					EntityFunctions.transferItemsBetweenEntities(entity, to, ignoremainhand);
				}
			}

			if (!(world instanceof ServerLevel)) {
				return true;
			}
			
			ServerLevel serverworld = (ServerLevel)world;
			
			if (ride) {
				SpawnEntityFunctions.startRidingEntityOnNextTick(serverworld, to, entity);
			}
			else {
				entity.remove(RemovalReason.DISCARDED);
			}

			to.addTag(CollectiveReference.MOD_ID + ".checked");
			SpawnEntityFunctions.spawnEntityOnNextTick(serverworld, to);

			return ride;
		}

		return true;
	}
}