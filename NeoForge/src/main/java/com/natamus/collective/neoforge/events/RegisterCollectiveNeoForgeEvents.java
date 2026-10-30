package com.natamus.collective.neoforge.events;

import com.natamus.collective.cmds.CommandCollective;
import com.natamus.collective.config.GenerateJSONFiles;
import com.natamus.collective.data.BlockEntityData;
import com.natamus.collective.events.CollectiveEvents;
import com.natamus.collective.events.CollectiveSpawnCheckEvents;
import com.natamus.collective.functions.WorldFunctions;
import com.natamus.collective.translations.ServerTranslationPack;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class RegisterCollectiveNeoForgeEvents {
	@SubscribeEvent
	public static void onServerStarted(ServerAboutToStartEvent e) {
		GenerateJSONFiles.initGeneration(e.getServer());
		ServerTranslationPack.onServerStarting(e.getServer());
		BlockEntityData.addModdedCampfiresToCache(e.getServer());
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
	public static void onWorldTick(LevelTickEvent.Post e) {
		Level level = e.getLevel();
		if (level.isClientSide()) {
			return;
		}

		CollectiveEvents.onWorldTick((ServerLevel)level);
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post e) {
		CollectiveEvents.onServerTick(e.getServer());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onMobSpawnerSpecialSpawn(FinalizeSpawnEvent e) {
		if (!e.getSpawnType().equals(EntitySpawnReason.SPAWNER)) {
			return;
		}

		e.getEntity().addTag(CollectiveReference.MOD_ID + ".fromspawner");
	}

	@SubscribeEvent
	public static void onMobPositionCheck(MobSpawnEvent.PositionCheck e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return;
		}

		EntitySpawnReason spawnReason = e.getSpawnType();
		boolean isSpawnerSpawn = spawnReason.equals(EntitySpawnReason.SPAWNER);
		if (!isSpawnerSpawn && !spawnReason.equals(EntitySpawnReason.NATURAL)) {
			return;
		}

		Mob mob = e.getEntity();
		if (isSpawnerSpawn) {
			mob.addTag(CollectiveReference.MOD_ID + ".fromspawner");
		}

		if (!CollectiveSpawnCheckEvents.onMobCheckSpawn(mob, (ServerLevel)level, null, spawnReason)) {
			e.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
		}
	}

	@SubscribeEvent
	public static void onEntityJoinLevel(EntityJoinLevelEvent e) {
		if (!CollectiveEvents.onEntityJoinLevel(e.getLevel(), e.getEntity())) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
		if (e.getEntity() instanceof ServerPlayer player) {
			ServerTranslationPack.onPlayerJoin(player);
		}
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent e) {
		CommandCollective.register(e.getDispatcher());
	}
}