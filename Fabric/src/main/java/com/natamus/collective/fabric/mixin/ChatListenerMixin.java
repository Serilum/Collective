package com.natamus.collective.fabric.mixin;

import com.mojang.authlib.GameProfile;
import com.natamus.collective.fabric.callbacks.CollectiveChatEvents;
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
    @Unique GameProfile collective$gameProfile;

    @Inject(method = "showMessageToPlayer", at = @At("HEAD"))
    public void captureParams(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, GameProfile gameProfile, boolean bl, Instant instant, CallbackInfoReturnable<Boolean> cir) {
        collective$bound = bound;
        collective$gameProfile = gameProfile;
    }

    @ModifyVariable(method = "showMessageToPlayer", at = @At("HEAD"), argsOnly = true)
    public Component modifyMessage(Component component) {
        try {
            return CollectiveChatEvents.CLIENT_CHAT_RECEIVED.invoker().onClientChat(collective$bound.chatType(), component, (collective$gameProfile != null ? collective$gameProfile.getId() : null));
        } finally {
            collective$bound = null;
            collective$gameProfile = null;
        }
    }
}