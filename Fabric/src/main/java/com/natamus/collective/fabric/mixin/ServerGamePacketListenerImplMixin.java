package com.natamus.collective.fabric.mixin;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.fabric.callbacks.CollectiveChatEvents;
import com.natamus.collective.functions.MessageFunctions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 1001)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "method_45167(Ljava/util/concurrent/CompletableFuture;Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/network/chat/Component;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void handleChat(CompletableFuture<?> completableFuture, PlayerChatMessage playerChatMessage, Component component, Executor executor, CallbackInfoReturnable<CompletableFuture<?>> cir) {
        Component message = Component.literal("<" + this.player.getName().getString() + "> " + playerChatMessage.decoratedContent().getString());

        Pair<Boolean, Component> pair = CollectiveChatEvents.SERVER_CHAT_RECEIVED.invoker().onServerChat(this.player, message, player.getUUID());
        if (pair != null) {
            Component newMessage = pair.getSecond();

            MessageFunctions.broadcastMessage(this.player.getCommandSenderWorld(), (MutableComponent) newMessage);
            cir.setReturnValue(completableFuture);
        }
    }
}
