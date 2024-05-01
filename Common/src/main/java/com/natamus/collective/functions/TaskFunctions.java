package com.natamus.collective.functions;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.events.CollectiveEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class TaskFunctions {
	public static void enqueueCollectiveTask(MinecraftServer minecraftServer, Runnable runnable, int delay) {
		CollectiveEvents.scheduledRunnables.add(new Pair<Integer, Runnable>(minecraftServer.getTickCount()+delay, runnable));
	}

	public static void enqueueImmediateTask(Level world, Runnable task, boolean allowClient) {
		if (world.isClientSide && allowClient) {
			task.run();
		}
		else {
			enqueueTask(world, task, 0);
		}
	}

	public static void enqueueTask(Level world, Runnable task, int delay) {
    	if (!(world instanceof ServerLevel)) {
    		return;
    	}

    	MinecraftServer server = ((ServerLevel)world).getServer();
    	server.submit(new TickTask(server.getTickCount() + delay, task));
	}
}
