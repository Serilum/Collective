package com.natamus.collective.neoforge.mixin;

import com.natamus.collective.events.CollectiveSpawnCheckEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = VillageSiege.class, priority = 1001)
public class VillageSiegeMixin {
	@ModifyVariable(method = "trySpawn(Lnet/minecraft/server/level/ServerLevel;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/ai/village/VillageSiege;findRandomSpawnPos(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/phys/Vec3;"))
	private Vec3 trySpawn_spawnPos(Vec3 spawnPos, ServerLevel serverLevel) {
		if (spawnPos == null) {
			return null;
		}

		if (!CollectiveSpawnCheckEvents.onSiegeZombieSpawn(serverLevel, spawnPos)) {
			return null;
		}
		return spawnPos;
	}
}
