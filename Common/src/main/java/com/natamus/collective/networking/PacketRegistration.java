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
		Network.registerPacket(ConfigSyncPacket.CHANNEL, ConfigSyncPacket.class, ConfigSyncPacket::encode, ConfigSyncPacket::decode, ConfigSyncPacket::handle);
		Network.registerPacket(ConfigServerRequestPacket.CHANNEL, ConfigServerRequestPacket.class, ConfigServerRequestPacket::encode, ConfigServerRequestPacket::decode, ConfigServerRequestPacket::handle);
		Network.registerPacket(ConfigServerValuesPacket.CHANNEL, ConfigServerValuesPacket.class, ConfigServerValuesPacket::encode, ConfigServerValuesPacket::decode, ConfigServerValuesPacket::handle);
		Network.registerPacket(ConfigServerSavePacket.CHANNEL, ConfigServerSavePacket.class, ConfigServerSavePacket::encode, ConfigServerSavePacket::decode, ConfigServerSavePacket::handle);
	}
}
