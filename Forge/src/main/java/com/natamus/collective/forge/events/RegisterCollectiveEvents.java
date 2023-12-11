package com.natamus.collective.forge.events;

import com.natamus.collective.events.CollectiveEvents;
import com.natamus.collective.functions.WorldFunctions;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class RegisterCollectiveEvents {
    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent e) {
        Level level = e.level;
        if (level.isClientSide || !e.phase.equals(Phase.END)) {
            return;
        }

        CollectiveEvents.onWorldTick((ServerLevel)level);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (!e.phase.equals(Phase.END)) {
            return;
        }

        CollectiveEvents.onServerTick(e.getServer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMobSpawnerSpecialSpawn(LivingSpawnEvent.SpecialSpawn e) {
        Level Level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
        if (Level == null) {
            return;
        }

        if (e.getSpawner() != null) {
            e.getEntity().addTag(CollectiveReference.MOD_ID + ".fromspawner");
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMobSpawnerCheckSpawn(LivingSpawnEvent.CheckSpawn e) {
        Level Level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
        if (Level == null) {
            return;
        }

        if (e.getSpawner() != null) {
            e.getEntity().addTag(CollectiveReference.MOD_ID + ".fromspawner");
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent e) {
        if (!CollectiveEvents.onEntityJoinLevel(e.getLevel(), e.getEntity())) {
            e.setCanceled(true);
        }
    }
}