package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.natamus.collective.fabric.callbacks.CollectivePlayerEvents;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(value = PlayerList.class, priority = 1001)
public class PlayerListMixin {
	@Inject(method = "placeNewPlayer", at = @At(value= "TAIL"))
	public void PlayerList_placeNewPlayer(Connection connection, ServerPlayer player, CallbackInfo ci) {
		CollectivePlayerEvents.PLAYER_LOGGED_IN.invoker().onPlayerLoggedIn(player.level, player);
	}
	
	@Inject(method = "remove", at = @At(value= "HEAD"))
	public void PlayerList_remove(ServerPlayer player, CallbackInfo ci) {
		CollectivePlayerEvents.PLAYER_LOGGED_OUT.invoker().onPlayerLoggedOut(player.level, player);
	}
}
