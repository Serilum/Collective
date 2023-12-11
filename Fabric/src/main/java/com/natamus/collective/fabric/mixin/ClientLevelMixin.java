package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveClientEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(value = ClientLevel.class, priority = 1001)
public class ClientLevelMixin {
	@Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientPacketListener;Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/Holder;IILjava/util/function/Supplier;Lnet/minecraft/client/renderer/LevelRenderer;ZJ)V", at = @At(value = "TAIL"))
	public void ClientLevelMixin_init(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey<?> resourceKey, Holder<?> holder, int i, int j, Supplier<?> supplier, LevelRenderer levelRenderer, boolean bl, long l, CallbackInfo ci) {
		CollectiveClientEvents.CLIENT_WORLD_LOAD.invoker().onClientWorldLoad((ClientLevel)(Object)this);
	}
}
