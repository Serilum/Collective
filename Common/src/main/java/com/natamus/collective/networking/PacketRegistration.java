package com.natamus.collective.networking;

import com.natamus.collective.implementations.networking.api.Network;
import com.natamus.collective.networking.packets.*;

public class PacketRegistration {

    public void init() {
        initClientPackets();
    }

    private void initClientPackets() {
        Network.registerPacket(CollectiveInstalledPacket.CHANNEL, CollectiveInstalledPacket.class, CollectiveInstalledPacket::encode, CollectiveInstalledPacket::decode, CollectiveInstalledPacket::handle);
        Network.registerPacket(EntityDataSyncPacket.CHANNEL, EntityDataSyncPacket.class, EntityDataSyncPacket::encode, EntityDataSyncPacket::decode, EntityDataSyncPacket::handle);
    }
}
