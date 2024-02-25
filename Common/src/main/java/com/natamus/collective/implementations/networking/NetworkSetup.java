package com.natamus.collective.implementations.networking;

import com.natamus.collective.implementations.networking.data.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.6
 *  by MysticDrew */

public class NetworkSetup {
    private final PacketRegistrationHandler packetRegistration;
    private static DelayedPacketRegistrationHandler delayedHandler;
    public static NetworkSetup INSTANCE;

    public NetworkSetup(PacketRegistrationHandler packetRegistration) {
        INSTANCE = this;
        this.packetRegistration = packetRegistration;
        getDelayedHandler().registerQueuedPackets(packetRegistration);
    }

    /**
     * Fabric does not enforce load order, so we may have to delay packet registrations.
     *
     * @return the handler;
     */
    public static DelayedPacketRegistrationHandler getDelayedHandler() {
        if (delayedHandler == null) {
            delayedHandler = new DelayedPacketRegistrationHandler();
        }
        return delayedHandler;
    }

    public static <T> PacketRegistrar registerPacket(ResourceLocation packetIdentifier, Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, Consumer<PacketContext<T>> handler) {
        if (INSTANCE != null) {
            return INSTANCE.packetRegistration.registerPacket(packetIdentifier, messageType, encoder, decoder, handler);
        }
        else {
            return getDelayedHandler().registerPacket(packetIdentifier, messageType, encoder, decoder, handler);
        }
    }

    public PacketRegistrationHandler getPacketRegistration() {
        return packetRegistration;
    }
}
