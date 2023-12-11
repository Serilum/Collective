package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveSpawnEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(value = EntityType.class, priority = 1001)
public class EntityTypeMixin<T extends Entity> {
	@Inject(method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	public void EntityType_spawn(ServerLevel serverLevel, CompoundTag compoundTag, Consumer<?> consumer, BlockPos blockPos, MobSpawnType mobSpawnType, boolean bl, boolean bl2, CallbackInfoReturnable<Entity> cir, Entity entity) {
		if (entity instanceof Mob) {
			if (!CollectiveSpawnEvents.MOB_SPECIAL_SPAWN.invoker().onMobSpecialSpawn((Mob)entity, serverLevel, blockPos, null, mobSpawnType)) {
				cir.setReturnValue(null);
			}
		}
	}
}
