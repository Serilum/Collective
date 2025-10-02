package com.natamus.collective.forge.events;

import com.natamus.collective.cmds.CommandCollective;
import com.natamus.collective.config.GenerateJSONFiles;
import com.natamus.collective.events.CollectiveEvents;
import com.natamus.collective.functions.WorldFunctions;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.invoke.MethodHandles;

public class RegisterCollectiveForgeEvents {
    public static void registerEventsInBus() {
        BusGroup.DEFAULT.register(MethodHandles.lookup(), RegisterCollectiveForgeEvents.class);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerAboutToStartEvent e) {
        GenerateJSONFiles.initGeneration(e.getServer());
    }

	@SubscribeEvent
	public static void onWorldLoad(LevelEvent.Load e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return;
		}

		CollectiveEvents.onWorldLoad(level);
	}

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent.Pre e) {
        Level level = e.level();
        if (level.isClientSide()) {
            return;
        }

        CollectiveEvents.onWorldTick((ServerLevel)level);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent.Post e) {
        CollectiveEvents.onServerTick(e.server());
    }

    @SubscribeEvent(priority = Priority.HIGHEST)
    public static void onMobSpawnerSpecialSpawn(MobSpawnEvent.FinalizeSpawn e) {
        Level Level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
        if (Level == null) {
            return;
        }

        if (!e.isSpawnCancelled()) {
            if (e.getSpawner() != null) {
                e.getEntity().addTag(CollectiveReference.MOD_ID + ".fromspawner");
            }
        }
    }

    @SubscribeEvent
    public static boolean onEntityJoinLevel(EntityJoinLevelEvent e) {
        return !CollectiveEvents.onEntityJoinLevel(e.getLevel(), e.getEntity());
    }

	@SubscribeEvent
	public static boolean onBlockBreak(BlockEvent.BreakEvent e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return false;
		}

        return !CollectiveEvents.onBlockBreak(level, e.getPlayer(), e.getPos(), e.getState(), null);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent e) {
    	CommandCollective.register(e.getDispatcher());
    }
}