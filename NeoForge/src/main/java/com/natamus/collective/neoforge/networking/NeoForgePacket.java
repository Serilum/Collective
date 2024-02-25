package com.natamus.collective.neoforge.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.6
 *  by MysticDrew */

public record NeoForgePacket<T>(NeoForgePacketContainer<T> container, T packet) implements CustomPacketPayload {
    @Override
    public void write(@NotNull FriendlyByteBuf buff) {
        container().encoder().accept(packet(), buff);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return container().packetIdentifier();
    }
}
