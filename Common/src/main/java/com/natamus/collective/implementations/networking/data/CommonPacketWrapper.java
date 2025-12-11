package com.natamus.collective.implementations.networking.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.8
 *  by MysticDrew */

public record CommonPacketWrapper<T>(PacketContainer<T> container, T packet) implements CustomPacketPayload {
    public void encode(FriendlyByteBuf buf) {
        container().encoder().accept(packet(), buf);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return container.type();
    }
}
