package com.natamus.collective.events;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.server.TickTask;

import java.util.concurrent.CopyOnWriteArrayList;

public class CollectiveClientEvents {
	private static final Minecraft mc = Minecraft.getInstance();
    public static CopyOnWriteArrayList<Pair<Integer, Runnable>> scheduledClientRunnables = new CopyOnWriteArrayList<>();

    public static int clientTickCount = 0;
    public static void onClientTick() {
		if (mc.isPaused()) {
			if (!(mc.gui.screen() instanceof DeathScreen)) {
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
}