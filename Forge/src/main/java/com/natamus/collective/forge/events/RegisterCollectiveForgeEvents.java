package com.natamus.collective.forge.events;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class RegisterCollectiveForgeEvents {
	@SubscribeEvent
	public void onServerStarted(ServerAboutToStartEvent e) {
		GenerateJSONFiles.initGeneration(e.getServer());
		ServerTranslationPack.onServerStarting(e.getServer());
		BlockEntityData.addModdedCampfiresToCache(e.getServer());
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
	public void onMobPositionCheck(MobSpawnEvent.PositionCheck e) {
		Level level = WorldFunctions.getWorldIfInstanceOfAndNotRemote(e.getLevel());
		if (level == null) {
			return;
		}

		MobSpawnType spawnReason = e.getSpawnType();
		boolean isSpawnerSpawn = spawnReason.equals(MobSpawnType.SPAWNER);
		if (!isSpawnerSpawn && !spawnReason.equals(MobSpawnType.NATURAL)) {
			return;
		}

		Mob mob = e.getEntity();
		if (isSpawnerSpawn) {
			mob.addTag(CollectiveReference.MOD_ID + ".fromspawner");
		}

		if (!CollectiveSpawnCheckEvents.onMobCheckSpawn(mob, (ServerLevel)level, null, spawnReason)) {
			e.setResult(Event.Result.DENY);
		}
	}

	@SubscribeEvent
	public void onEntityJoinLevel(EntityJoinLevelEvent e) {
		if (!CollectiveEvents.onEntityJoinLevel(e.getLevel(), e.getEntity())) {
			e.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onEntityBlockPlace(BlockEvent.EntityPlaceEvent e) {
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
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent e) {
		if (e.getEntity() instanceof ServerPlayer player) {
			ServerTranslationPack.onPlayerJoin(player);
		}
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent e) {
		CommandCollective.register(e.getDispatcher());
	}
}
