package com.natamus.collective.networking.packets;

import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class CollectiveInstalledPacket {
    public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(CollectiveReference.MOD_ID, "collective_installed_packet");

    public CollectiveInstalledPacket() {
    }

    public static CollectiveInstalledPacket decode(FriendlyByteBuf buf) {
        return new CollectiveInstalledPacket();
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(PacketContext<CollectiveInstalledPacket> ctx) {
    }
}
