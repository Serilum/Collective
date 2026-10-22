package com.natamus.collective.events;

import com.natamus.collective.globalcallbacks.CollectiveSpawnCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class CollectiveSpawnCheckEvents {
	public static boolean onMobCheckSpawn(Mob mob, ServerLevel level, BlockPos spawnerPos, EntitySpawnReason spawnReason) {
		if (mob == null) {
			return true;
		}

		return CollectiveSpawnCallback.ON_MOB_CHECK_SPAWN.invoker().onMobCheckSpawn(mob, level, spawnerPos, spawnReason);
	}

	public static boolean onPatrolMemberSpawn(ServerLevel level, BlockPos pos) {
		return onUnspawnedMobCheckSpawn(EntityTypes.PILLAGER, level, new Vec3(pos.getX(), pos.getY(), pos.getZ()), EntitySpawnReason.PATROL);
	}

	public static boolean onSiegeZombieSpawn(ServerLevel level, Vec3 spawnPos) {
		return onUnspawnedMobCheckSpawn(EntityTypes.ZOMBIE, level, spawnPos, EntitySpawnReason.EVENT);
	}

	public static boolean onNetherPortalSpawn(ServerLevel level, BlockPos spawnPos) {
		return onUnspawnedMobCheckSpawn(EntityTypes.ZOMBIFIED_PIGLIN, level, Vec3.atBottomCenterOf(spawnPos), EntitySpawnReason.STRUCTURE);
	}

	private static boolean onUnspawnedMobCheckSpawn(EntityType<? extends Mob> entityType, ServerLevel level, Vec3 spawnPos, EntitySpawnReason spawnReason) {
		Mob mob = entityType.create(level, spawnReason);
		if (mob == null) {
			return true;
		}

		mob.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
		return onMobCheckSpawn(mob, level, null, spawnReason);
	}
}
