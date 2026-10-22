package com.natamus.collective.globalcallbacks;

import com.natamus.collective.implementations.event.Event;
import com.natamus.collective.implementations.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

public class CollectiveSpawnCallback {
	private CollectiveSpawnCallback() { }

	public static final Event<On_Mob_Check_Spawn> ON_MOB_CHECK_SPAWN = EventFactory.createArrayBacked(On_Mob_Check_Spawn.class, callbacks -> (mob, level, spawnerPos, spawnReason) -> {
		for (On_Mob_Check_Spawn callback : callbacks) {
			if (!callback.onMobCheckSpawn(mob, level, spawnerPos, spawnReason)) {
				return false;
			}
		}

		return true;
	});

	@FunctionalInterface
	public interface On_Mob_Check_Spawn {
		 boolean onMobCheckSpawn(Mob mob, ServerLevel level, BlockPos spawnerPos, MobSpawnType spawnReason);
	}
}
