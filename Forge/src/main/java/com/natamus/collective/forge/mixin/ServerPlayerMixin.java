package com.natamus.collective.forge.mixin;

import com.natamus.collective.data.IEntityDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, priority = 1001)
public class ServerPlayerMixin {
	@Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At(value = "TAIL"))
	public void ServerPlayer_restoreFrom(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
		CompoundTag stored = ((IEntityDataHolder)oldPlayer).collective_getStored();
		((IEntityDataHolder)this).collective_setStored(stored);
	}
}
