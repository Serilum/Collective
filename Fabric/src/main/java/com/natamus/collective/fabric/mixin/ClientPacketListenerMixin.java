package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.natamus.collective.fabric.callbacks.CollectiveChatEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChatPacket;

@Mixin(value = ClientPacketListener.class, priority = 1001)
public class ClientPacketListenerMixin {
	@Final @Shadow private Minecraft minecraft;
	
	@Inject(method = "handleChat(Lnet/minecraft/network/protocol/game/ClientboundChatPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;handleChat(Lnet/minecraft/network/chat/ChatType;Lnet/minecraft/network/chat/Component;Ljava/util/UUID;)V"), cancellable = true)
	public void ClientPacketListener_handleChat(ClientboundChatPacket clientboundChatPacket, CallbackInfo ci) {
		Component message = clientboundChatPacket.getMessage();
		Component newMessage = CollectiveChatEvents.CLIENT_CHAT_RECEIVED.invoker().onClientChat(clientboundChatPacket.getType(), message, clientboundChatPacket.getSender());
		if (message != newMessage) {
			minecraft.gui.handleChat(clientboundChatPacket.getType(), newMessage, clientboundChatPacket.getSender());
			ci.cancel();
		}
	}
}
