package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveChatEvents;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;

@Mixin(value = ChatListener.class, priority = 1001)
public abstract class ChatListenerMixin {
    @Unique ChatType.Bound collective$bound;
    @Unique PlayerInfo collective$playerInfo;

    @Inject(method = "showMessageToPlayer", at = @At("HEAD"))
    public void captureParams(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, PlayerInfo playerInfo, boolean bl, Instant instant, CallbackInfoReturnable<Boolean> cir) {
        collective$bound = bound;
        collective$playerInfo = playerInfo;
    }

    @ModifyVariable(method = "showMessageToPlayer", at = @At("HEAD"), argsOnly = true)
    public Component modifyMessage(Component component) {
        try {
            return CollectiveChatEvents.CLIENT_CHAT_RECEIVED.invoker().onClientChat(collective$bound.chatType(), component, (collective$playerInfo != null ? collective$playerInfo.getProfile().getId() : null));
        } finally {
            collective$bound = null;
            collective$playerInfo = null;
        }
    }
}