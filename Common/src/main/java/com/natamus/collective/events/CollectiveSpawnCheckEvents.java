package com.natamus.collective.events;

import com.natamus.collective.globalcallbacks.CollectiveSpawnCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;

public class CollectiveSpawnCheckEvents {
	public static boolean onMobCheckSpawn(Mob mob, ServerLevel level, BlockPos spawnerPos, MobSpawnType spawnReason) {
		if (mob == null) {
			return true;
		}

		return CollectiveSpawnCallback.ON_MOB_CHECK_SPAWN.invoker().onMobCheckSpawn(mob, level, spawnerPos, spawnReason);
	}

	public static boolean onPatrolMemberSpawn(ServerLevel level, BlockPos pos) {
		return onUnspawnedMobCheckSpawn(EntityType.PILLAGER, level, new Vec3(pos.getX(), pos.getY(), pos.getZ()), MobSpawnType.PATROL);
	}

	public static boolean onSiegeZombieSpawn(ServerLevel level, Vec3 spawnPos) {
		return onUnspawnedMobCheckSpawn(EntityType.ZOMBIE, level, spawnPos, MobSpawnType.EVENT);
	}

	public static boolean onNetherPortalSpawn(ServerLevel level, BlockPos spawnPos) {
		return onUnspawnedMobCheckSpawn(EntityType.ZOMBIFIED_PIGLIN, level, Vec3.atBottomCenterOf(spawnPos), MobSpawnType.STRUCTURE);
	}

	private static boolean onUnspawnedMobCheckSpawn(EntityType<? extends Mob> entityType, ServerLevel level, Vec3 spawnPos, MobSpawnType spawnReason) {
		Mob mob = entityType.create(level);
		if (mob == null) {
			return true;
		}

		mob.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
		return onMobCheckSpawn(mob, level, null, spawnReason);
	}
}
