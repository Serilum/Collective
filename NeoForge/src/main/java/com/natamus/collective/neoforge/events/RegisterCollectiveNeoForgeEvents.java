package com.natamus.collective.neoforge.events;

import com.natamus.collective.cmds.CommandCollective;
import com.natamus.collective.config.GenerateJSONFiles;
import com.natamus.collective.events.CollectiveEvents;
import com.natamus.collective.functions.WorldFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class RegisterCollectiveNeoForgeEvents {
    @SubscribeEvent
    public static void onServerStarted(ServerAboutToStartEvent e) {
        GenerateJSONFiles.initGeneration(e.getServer());
    }

    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post e) {
        Level level = e.getLevel();
        if (level.isClientSide) {
            return;
        }

        CollectiveEvents.onWorldTick((ServerLevel)level);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        CollectiveEvents.onServerTick(e.getServer());
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent e) {
        if (!CollectiveEvents.onEntityJoinLevel(e.getLevel(), e.getEntity())) {
            e.setCanceled(true);
        }
    }

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return;
		}

		if (!CollectiveEvents.onBlockBreak(level, e.getPlayer(), e.getPos(), e.getState(), null)) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent e) {
		CommandCollective.register(e.getDispatcher());
	}
}