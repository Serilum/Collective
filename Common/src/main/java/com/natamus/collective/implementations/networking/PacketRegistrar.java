package com.natamus.collective.implementations.networking;

import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.6
 *  by MysticDrew */

public interface PacketRegistrar {
    /**
     * @return the side
     */
    Side getSide();

    /**
     * Packet Registration
     *
     * @param packetIdentifier - The unique {@link ResourceLocation} packet id.
     * @param messageType      - The class of the packet.
     * @param encoder          - The encoder method.
     * @param decoder          - The decoder method.
     * @param handler          - The handler method.
     * @param <T>              - The class type
     * @return The registrar for chaining registrations.
     */
    <T> PacketRegistrar registerPacket(ResourceLocation packetIdentifier, Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, Consumer<PacketContext<T>> handler);
}
