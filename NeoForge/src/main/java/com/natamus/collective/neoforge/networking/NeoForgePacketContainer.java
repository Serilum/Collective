package com.natamus.collective.neoforge.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.function.BiConsumer;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.6
 *  by MysticDrew */

public record NeoForgePacketContainer<T>(Class<T> messageType,
                                         ResourceLocation packetIdentifier,
                                         BiConsumer<T, FriendlyByteBuf> encoder,
                                         FriendlyByteBuf.Reader<NeoForgePacket<T>> decoder,
                                         IPayloadHandler<NeoForgePacket<T>> handler)
{
}
