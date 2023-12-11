package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;

public class CollectiveSpawnEvents {
	private CollectiveSpawnEvents() { }

    public static final Event<Mob_Check_Spawn> MOB_CHECK_SPAWN = EventFactory.createArrayBacked(Mob_Check_Spawn.class, callbacks -> (entity, world, spawnerPos, spawnReason) -> {
        for (Mob_Check_Spawn callback : callbacks) {
        	if (!callback.onMobCheckSpawn(entity, world, spawnerPos, spawnReason)) {
        		return false;
        	}
        }

        return true;
    });

    public static final Event<Mob_Special_Spawn> MOB_SPECIAL_SPAWN = EventFactory.createArrayBacked(Mob_Special_Spawn.class, callbacks -> (entity, level, blockPos, spawner, spawnReason) -> {
        for (Mob_Special_Spawn callback : callbacks) {
        	if (!callback.onMobSpecialSpawn(entity, level, blockPos, spawner, spawnReason)) {
        		return false;
        	}
        }

        return true;
    });

	@FunctionalInterface
	public interface Mob_Check_Spawn {
		 boolean onMobCheckSpawn(Mob entity, ServerLevel world, BlockPos spawnerPos, MobSpawnType spawnReason);
	}

	@FunctionalInterface
	public interface Mob_Special_Spawn {
		 boolean onMobSpecialSpawn(Mob entity, Level level, BlockPos blockPos, BaseSpawner spawner, MobSpawnType spawnReason);
	}
}