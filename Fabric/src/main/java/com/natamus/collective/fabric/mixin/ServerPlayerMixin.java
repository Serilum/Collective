package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveItemEvents;
import com.natamus.collective.fabric.callbacks.CollectivePlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayer.class, priority = 1001)
public class ServerPlayerMixin {
	@Inject(method = "tick()V", at = @At(value = "HEAD"))
	public void ServerPlayer_tick(CallbackInfo ci) {
		ServerPlayer serverPlayer = (ServerPlayer)(Object)this;

		CollectivePlayerEvents.PLAYER_TICK.invoker().onTick(serverPlayer.serverLevel(), serverPlayer);
	}
	
	@Inject(method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At(value = "HEAD"))
	public void ServerPlayer_die(DamageSource damageSource, CallbackInfo ci) {
		ServerPlayer serverPlayer = (ServerPlayer)(Object)this;

		CollectivePlayerEvents.PLAYER_DEATH.invoker().onDeath(serverPlayer.serverLevel(), serverPlayer);
	}
	
	@Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(value = "RETURN"))
	public void ServerPlayer_teleport(TeleportTransition teleportTransition, CallbackInfoReturnable<ServerPlayer> cir) {
		ServerPlayer serverPlayer = (ServerPlayer)(Object)this;
		
		CollectivePlayerEvents.PLAYER_CHANGE_DIMENSION.invoker().onChangeDimension(serverPlayer.serverLevel(), serverPlayer);
	}
	
	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private void ServerPlayer_drop(ItemStack itemStack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> ci) {
		ServerPlayer serverPlayer = (ServerPlayer)(Object)this;
		CollectiveItemEvents.ON_ITEM_TOSSED.invoker().onItemTossed(serverPlayer, itemStack);
	}
}
