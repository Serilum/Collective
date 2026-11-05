package com.natamus.collective.networking.packets;

import com.natamus.collective.config.DuskConfigSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class ConfigSyncClientHandler {
	private static ClientPacketListener serverValuesConnection = null;

	public static void apply(String modid, String serverValues) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.isLocalServer()) {
			return;
		}

		restoreOwnValuesAfterLeavingServer();
		serverValuesConnection = mc.getConnection();
		DuskConfigSync.useServerValues(modid, serverValues);
	}

	public static void restoreOwnValuesAfterLeavingServer() {
		if (!DuskConfigSync.isUsingServerValues()) {
			return;
		}

		if (Minecraft.getInstance().getConnection() != serverValuesConnection) {
			DuskConfigSync.restoreOwnValues();
			serverValuesConnection = null;
		}
	}
}
