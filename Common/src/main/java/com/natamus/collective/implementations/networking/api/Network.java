package com.natamus.collective.implementations.networking.api;

import com.natamus.collective.implementations.networking.NetworkSetup;
import com.natamus.collective.implementations.networking.PacketRegistrar;
import com.natamus.collective.implementations.networking.data.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.6
 *  by MysticDrew */

public class Network {
    /**
     * Packet Registration
     *
     * @param packetIdentifier - The unique {@link ResourceLocation} packet id.
     * @param messageType      - The class of the packet.
     * @param encoder          - The encoder method.
     * @param decoder          - The decoder method.
     * @param handler          - The handler method.
     * @param <T>              - The type
     * @return The registrar for chaining registrations.
     */
    public static <T> PacketRegistrar registerPacket(ResourceLocation packetIdentifier, Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, Consumer<PacketContext<T>> handler) {
        return NetworkSetup.registerPacket(packetIdentifier, messageType, encoder, decoder, handler);
    }

    /**
     * Gets the Network handler for use to send packets.
     *
     * @return - The network handler
     */
    public static NetworkHandler getNetworkHandler() {
        return NetworkSetup.INSTANCE.getPacketRegistration();
    }
}
