package com.natamus.collective.implementations.networking.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.8
 *  by MysticDrew */

public record PacketContainer<T>(CustomPacketPayload.Type<? extends CustomPacketPayload> type, Class<T> classType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, Consumer<PacketContext<T>> handler) {
    public PacketContainer(ResourceLocation id, Class<T> classType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, Consumer<PacketContext<T>> handle) {
        this(new CustomPacketPayload.Type<>(id), classType, encoder, decoder, handle);
    }

    @SuppressWarnings("unchecked")
    public <K extends CustomPacketPayload> CustomPacketPayload.Type<K> getType() {
        return (CustomPacketPayload.Type<K>) type();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
	public StreamCodec<FriendlyByteBuf, CommonPacketWrapper> getCodec() {
        return CustomPacketPayload.codec((packet, buf) -> this.encoder().accept((T)packet.packet(), buf), (buf) -> new CommonPacketWrapper<>(this, this.decoder().apply(buf)));
    }
}
