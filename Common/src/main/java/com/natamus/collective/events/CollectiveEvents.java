package com.natamus.collective.events;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.datafixers.util.Pair;
import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.data.Constants;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.features.PlayerHeadCacheFeature;
import com.natamus.collective.functions.BlockPosFunctions;
import com.natamus.collective.functions.EntityFunctions;
import com.natamus.collective.functions.HeadFunctions;
import com.natamus.collective.functions.SpawnEntityFunctions;
import com.natamus.collective.objects.SAMObject;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectiveEvents {
	public static WeakHashMap<ServerLevel, List<Entity>> entitiesToSpawn = new WeakHashMap<ServerLevel, List<Entity>>();
	public static WeakHashMap<ServerLevel, WeakHashMap<Entity, Entity>> entitiesToRide = new WeakHashMap<ServerLevel, WeakHashMap<Entity, Entity>>();
	public static CopyOnWriteArrayList<Pair<Integer, Runnable>> scheduledRunnables = new CopyOnWriteArrayList<Pair<Integer, Runnable>>();

	public static void onWorldLoad(Level level) {
		Constants.initConstantData(level);
	}

	public static void onWorldTick(ServerLevel serverLevel) {
		if (entitiesToSpawn.computeIfAbsent(serverLevel, k -> new ArrayList<Entity>()).size() > 0) {
			Entity tospawn = entitiesToSpawn.get(serverLevel).getFirst();

			serverLevel.addFreshEntityWithPassengers(tospawn);

			if (entitiesToRide.computeIfAbsent(serverLevel, k -> new WeakHashMap<Entity, Entity>()).containsKey(tospawn)) {
				Entity rider = entitiesToRide.get(serverLevel).get(tospawn);

				rider.startRiding(tospawn);

				entitiesToRide.get(serverLevel).remove(tospawn);
			}

			entitiesToSpawn.get(serverLevel).removeFirst();
		}
	}

	public static void onServerTick(MinecraftServer minecraftServer) {
		int serverTickCount = minecraftServer.getTickCount();
		for (Pair<Integer, Runnable> pair : scheduledRunnables) {
			if (pair.getFirst() <= serverTickCount) {
				minecraftServer.addTickable(new TickTask(minecraftServer.getTickCount(), pair.getSecond()));
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

			Entity to = sam.toEntityType.create(level, EntitySpawnReason.NATURAL);
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

	public static boolean onBlockBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (level.isClientSide) {
			return true;
		}

		Block block = state.getBlock();
		if (block instanceof SkullBlock || block instanceof WallSkullBlock) {
			if (player.isCreative()) {
				return true;
			}

			BlockEntity targetBlockEntity = level.getBlockEntity(pos);
			if (!(targetBlockEntity instanceof SkullBlockEntity)) {
				return true;
			}

			SkullBlockEntity skullBlockEntity = (SkullBlockEntity)targetBlockEntity;
			if (skullBlockEntity == null) {
				return true;
			}

			ResolvableProfile resolvableProfile = skullBlockEntity.getOwnerProfile();
			if (resolvableProfile == null) {
				return true;
			}

			GameProfile gameProfile = resolvableProfile.gameProfile();
			if (gameProfile == null) {
				return true;
			}

			UUID uuid = gameProfile.getId();
			if (uuid.toString().startsWith("ffffffff")) { // Old player head format
				return true;
			}

			ItemStack namedHeadStack = null;
			if (!gameProfile.getName().equals("")) {
				namedHeadStack = HeadFunctions.getNewPlayerHead(gameProfile, 1);
			}

			if (namedHeadStack == null) {
				GameProfile uuidGameProfile;
				if (PlayerHeadCacheFeature.cachedGameProfileMap.containsKey(uuid)) {
					uuidGameProfile = PlayerHeadCacheFeature.cachedGameProfileMap.get(uuid);
				} else {
					MinecraftSessionService minecraftSessionService = ((ServerLevel)level).getServer().getSessionService();

					ProfileResult uuidProfileResult = minecraftSessionService.fetchProfile(uuid, false);
					if (uuidProfileResult == null) {
						return true;
					}

					uuidGameProfile = uuidProfileResult.profile();

					if (uuidGameProfile.getName().equals("")) {
						return true;
					}

					PlayerHeadCacheFeature.cachedGameProfileMap.put(uuid, uuidGameProfile);
				}

				namedHeadStack = HeadFunctions.getNewPlayerHead(uuidGameProfile, 1);
			}

			if (namedHeadStack != null) {
				level.destroyBlock(pos, false);

				level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY()+0.5, pos.getZ(), namedHeadStack));
				return false;
			}
		}

		return true;
	}
}