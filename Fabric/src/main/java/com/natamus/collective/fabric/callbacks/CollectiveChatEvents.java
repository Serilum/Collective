package com.natamus.collective.fabric.callbacks;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class CollectiveChatEvents {
	private CollectiveChatEvents() { }
	 
    public static final Event<On_Client_Chat> CLIENT_CHAT_RECEIVED = EventFactory.createArrayBacked(On_Client_Chat.class, callbacks -> (chatType, message, senderUUID) -> {
        for (On_Client_Chat callback : callbacks) {
        	Component newMessage = callback.onClientChat(chatType, message, senderUUID);
        	if (newMessage != message) {
        		return newMessage;
        	}
        }
        
        return message;
    });
    
    public static final Event<On_Server_Chat> SERVER_CHAT_RECEIVED = EventFactory.createArrayBacked(On_Server_Chat.class, callbacks -> (serverPlayer, message, senderUUID) -> {
        for (On_Server_Chat callback : callbacks) {
        	Pair<Boolean, Component> pair = callback.onServerChat(serverPlayer, message, senderUUID);
        	if (pair != null) {
	        	if (pair.getFirst()) {
	        		return pair;
	        	}
        	}
        }
        
        return null;
    });
    
	@FunctionalInterface
	public interface On_Client_Chat {
		 Component onClientChat(ChatType chatType, Component message, UUID senderUUID);
	}
	
	@FunctionalInterface
	public interface On_Server_Chat {
		 Pair<Boolean, Component> onServerChat(ServerPlayer serverPlayer, Component message, UUID senderUUID);
	}
}
