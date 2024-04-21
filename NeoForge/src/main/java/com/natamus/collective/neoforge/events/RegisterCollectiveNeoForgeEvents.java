package com.natamus.collective.neoforge.events;

import com.natamus.collective.cmds.CommandCollective;
import com.natamus.collective.config.GenerateJSONFiles;
import com.natamus.collective.events.CollectiveEvents;
import com.natamus.collective.functions.WorldFunctions;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@EventBusSubscriber
public class RegisterCollectiveNeoForgeEvents {
    @SubscribeEvent
    public static void onServerStarted(ServerAboutToStartEvent e) {
        GenerateJSONFiles.initGeneration(e.getServer());
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent e) {
        Level level = e.level;
        if (level.isClientSide || !e.phase.equals(TickEvent.Phase.END)) {
            return;
        }

        CollectiveEvents.onWorldTick((ServerLevel)level);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (!e.phase.equals(TickEvent.Phase.END)) {
            return;
        }

        CollectiveEvents.onServerTick(e.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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
	public static void onEntityBlockPlace(BlockEvent.EntityPlaceEvent e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return;
		}

		Entity entity = e.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}

		Player player = (Player)entity;
		ItemStack itemStack = player.getMainHandItem();
		if (itemStack.isEmpty()) {
			itemStack = player.getOffhandItem();
		}

		if (!CollectiveEvents.onEntityBlockPlace(level, e.getPos(), e.getPlacedBlock(), (LivingEntity)entity, itemStack)) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent e) {
		CommandCollective.register(e.getDispatcher());
	}
}