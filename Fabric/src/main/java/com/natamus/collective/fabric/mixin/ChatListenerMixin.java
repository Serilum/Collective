package com.natamus.collective.fabric.mixin;

import com.mojang.authlib.GameProfile;
import com.natamus.collective.fabric.callbacks.CollectiveChatEvents;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.time.Instant;

@Mixin(value = ChatListener.class, priority = 1001)
public abstract class ChatListenerMixin {
    @Shadow private @Final Minecraft minecraft;
    @Shadow private long previousMessageTime;
    @Shadow protected abstract void narrateChatMessage(ChatType.Bound bound, Component component);
    @Shadow protected abstract void logPlayerMessage(PlayerChatMessage playerChatMessage, ChatType.Bound bound, GameProfile gameProfile, ChatTrustLevel chatTrustLevel);
    @Shadow protected abstract boolean showMessageToPlayer(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, GameProfile gameProfile, boolean bl, Instant instant);

    @Inject(method = "showMessageToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getChat()Lnet/minecraft/client/gui/components/ChatComponent;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void showMessageToPlayer(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, GameProfile gameProfile, boolean bl, Instant instant, CallbackInfoReturnable<Boolean> cir, ChatTrustLevel chatTrustLevel, GuiMessageTag guiMessageTag, MessageSignature messageSignature, FilterMask filterMask) {
        Component newMessage = CollectiveChatEvents.CLIENT_CHAT_RECEIVED.invoker().onClientChat(bound.chatType(), component, gameProfile.getId());
		if (component != newMessage) {
            this.minecraft.gui.getChat().addMessage(newMessage, messageSignature, guiMessageTag);
            this.narrateChatMessage(bound, playerChatMessage.decoratedContent());
            this.logPlayerMessage(playerChatMessage, bound, gameProfile, chatTrustLevel);
            this.previousMessageTime = Util.getMillis();
            cir.setReturnValue(true);
		}
    }
}
