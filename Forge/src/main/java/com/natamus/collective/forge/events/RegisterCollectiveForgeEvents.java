package com.natamus.collective.forge.events;

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
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class RegisterCollectiveForgeEvents {
    @SubscribeEvent
    public void onServerStarted(ServerAboutToStartEvent e) {
        GenerateJSONFiles.initGeneration(e.getServer());
    }

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
    public void onMobSpawnerSpecialSpawn(MobSpawnEvent.FinalizeSpawn e) {
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
    public void onEntityJoinLevel(EntityJoinLevelEvent e) {
        if (!CollectiveEvents.onEntityJoinLevel(e.getLevel(), e.getEntity())) {
            e.setCanceled(true);
        }
    }

	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return;
		}

		if (!CollectiveEvents.onBlockBreak(level, e.getPlayer(), e.getPos(), e.getState(), null)) {
			e.setCanceled(true);
		}
	}

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent e) {
    	CommandCollective.register(e.getDispatcher());
    }
}