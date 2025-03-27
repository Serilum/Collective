package com.natamus.collective.fabric.mixin;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.fabric.callbacks.CollectiveChatEvents;
import com.natamus.collective.functions.MessageFunctions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 1001)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "method_45064(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/network/chat/Component;Lnet/minecraft/server/network/FilteredText;)V", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    public void handleChat(PlayerChatMessage playerChatMessage, Component component, FilteredText filteredText, CallbackInfo ci) {
        Component message = Component.literal("<" + this.player.getName().getString() + "> " + playerChatMessage.decoratedContent().getString());

        Pair<Boolean, Component> pair = CollectiveChatEvents.SERVER_CHAT_RECEIVED.invoker().onServerChat(this.player, message, player.getUUID());
        if (pair != null) {
            Component newMessage = pair.getSecond();

            MessageFunctions.broadcastMessage(this.player.getCommandSenderWorld(), (MutableComponent) newMessage);
            ci.cancel();
        }
    }
}
