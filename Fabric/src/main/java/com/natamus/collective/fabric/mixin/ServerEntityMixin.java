package com.natamus.collective.fabric.mixin;

import com.natamus.collective.networking.packets.EntityDataSyncPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerEntity.class, priority = 1001)
public class ServerEntityMixin {
	@Shadow @Final private Entity entity;

	@Inject(method = "addPairing(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At(value = "TAIL"))
	public void ServerEntity_addPairing(ServerPlayer player, CallbackInfo ci) {
		EntityDataSyncPacket.syncToPlayer(entity, player);
	}
}
