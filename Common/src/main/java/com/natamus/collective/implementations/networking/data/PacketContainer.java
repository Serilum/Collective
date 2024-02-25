package com.natamus.collective.implementations.networking.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.6
 *  by MysticDrew */

public record PacketContainer<T>(ResourceLocation packetIdentifier,
                                 Class<T> messageType,
                                 BiConsumer<T, FriendlyByteBuf> encoder,
                                 Function<FriendlyByteBuf, T> decoder,
                                 Consumer<PacketContext<T>> handler)
{
}
