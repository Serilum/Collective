package com.natamus.collective.functions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;

public class WorldFunctions {
	public static void setWorldTime(ServerLevel serverLevel, Integer time) {
		if (time < 0 || time > 24000) {
			return;
		}

		int days = getTotalDaysPassed(serverLevel);
		serverLevel.setDayTime(time + (days * 24000L));
	}
	
	public static int getTotalTimePassed(ServerLevel serverLevel) {
		return (int)serverLevel.getDayTime();
	}
	public static int getTotalDaysPassed(ServerLevel serverLevel) {
		int currenttime = getTotalTimePassed(serverLevel);
		return (int)Math.floor((double)currenttime/24000);
	}
	public static int getWorldTime(ServerLevel serverLevel) {
		return getTotalTimePassed(serverLevel) - (getTotalDaysPassed(serverLevel)*24000);
	}
	
	// Dimension functions
	public static String getWorldDimensionName(Level level) {
		return level.dimension().location().toString();
	}
	public static boolean isOverworld(Level level) {
		return getWorldDimensionName(level).toLowerCase().endsWith("overworld");
	}
	public static boolean isNether(Level level) {
		return getWorldDimensionName(level).toLowerCase().endsWith("nether");
	}
	public static boolean isEnd(Level level) {
		return getWorldDimensionName(level).toLowerCase().endsWith("end");
	}
	
	// LevelAccessor/IWorld functions
	public static Level getWorldIfInstanceOfAndNotRemote(LevelAccessor levelAccessor) {
		if (levelAccessor.isClientSide()) {
			return null;
		}
		if (levelAccessor instanceof Level) {
			return ((Level)levelAccessor);
		}
		return null;
	}
	
	// Path
	public static String getWorldPath(ServerLevel serverLevel) {
		return getWorldPath(serverLevel.getServer());
	}
    public static String getWorldPath(MinecraftServer minecraftServer) {
        String worldpath = minecraftServer.getWorldPath(LevelResource.ROOT).toString();
        return worldpath.substring(0, worldpath.length() - 2);
    }

	public static String getWorldFolderName(ServerLevel serverLevel) {
		return getWorldFolderName(serverLevel.getServer());
	}
	public static String getWorldFolderName(MinecraftServer minecraftServer) {
		String worldPath = getWorldPath(minecraftServer);
		return worldPath.substring(worldPath.lastIndexOf(File.separator) + 1);
	}
}
