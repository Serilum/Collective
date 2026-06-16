package com.natamus.collective;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.cmds.CommandCollective;
import com.natamus.collective.config.GenerateJSONFiles;
import com.natamus.collective.events.CollectiveEvents;
import com.natamus.collective.fabric.callbacks.CollectivePlayerEvents;
import com.natamus.collective.fabric.networking.FabricNetworkHandler;
import com.natamus.collective.implementations.networking.NetworkSetup;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.translations.ServerTranslationPack;
import com.natamus.collective.util.CollectiveReference;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class CollectiveFabric implements ModInitializer { 
	@Override
	public void onInitialize() {
		new NetworkSetup(new FabricNetworkHandler(Side.SERVER));

		setGlobalConstants();
		CollectiveCommon.init();

		ServerLifecycleEvents.SERVER_STARTING.register((minecraftServer) -> {
			GenerateJSONFiles.initGeneration(minecraftServer);
			ServerTranslationPack.onServerStarting(minecraftServer);
		});

		ServerWorldEvents.LOAD.register((MinecraftServer server, ServerLevel level) -> {
			CollectiveEvents.onWorldLoad(level);
		});

		ServerTickEvents.START_WORLD_TICK.register((serverLevel) -> {
			CollectiveEvents.onWorldTick(serverLevel);
		});

		ServerTickEvents.START_SERVER_TICK.register((minecraftServer) -> {
			CollectiveEvents.onServerTick(minecraftServer);
		});
		
		ServerEntityEvents.ENTITY_LOAD.register((entity, serverLevel) -> {
			CollectiveEvents.onEntityJoinLevel(serverLevel, entity);
		});

		CollectivePlayerEvents.PLAYER_LOGGED_IN.register((world, player) -> {
			if (player instanceof ServerPlayer serverPlayer) {
				ServerTranslationPack.onPlayerJoin(serverPlayer);
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CommandCollective.register(dispatcher);
		});

		RegisterMod.register(CollectiveReference.NAME, CollectiveReference.MOD_ID, CollectiveReference.VERSION, CollectiveReference.ACCEPTED_VERSIONS);
	}

	private static void setGlobalConstants() {

	}
}
