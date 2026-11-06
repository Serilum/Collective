package com.natamus.collective.events;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.config.screen.DuskConfigRegistryCache;
import com.natamus.collective.networking.packets.ConfigSyncClientHandler;
import com.natamus.collective.worldcreation.WorldCreationRows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.server.TickTask;

import java.util.concurrent.CopyOnWriteArrayList;

public class CollectiveClientEvents {
	private static final Minecraft mc = Minecraft.getInstance();
	public static CopyOnWriteArrayList<Pair<Integer, Runnable>> scheduledClientRunnables = new CopyOnWriteArrayList<>();

	private static ClientLevel lastPickerCacheLevel = null;

	public static int clientTickCount = 0;
	public static void onClientTick() {
		cachePickerIdsOnWorldJoin();
		ConfigSyncClientHandler.restoreOwnValuesAfterLeavingServer();

		if (mc.isPaused()) {
			if (!(mc.screen instanceof DeathScreen)) {
				return;
			}
		}

		for (Pair<Integer, Runnable> pair : scheduledClientRunnables) {
			if (pair.getFirst() <= clientTickCount) {
				Minecraft.getInstance().execute(new TickTask(clientTickCount, pair.getSecond()));
				scheduledClientRunnables.remove(pair);
			}
		}

		clientTickCount++;
	}

	private static void cachePickerIdsOnWorldJoin() {
		if (mc.level == lastPickerCacheLevel) {
			return;
		}

		lastPickerCacheLevel = mc.level;
		if (mc.level != null) {
			WorldCreationRows.forgetCreateWorldScreen();
			DuskConfigRegistryCache.saveWorldRegistryIds();
		}
	}
}