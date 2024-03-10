package com.natamus.collective;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.cmds.CommandCollective;
import com.natamus.collective.config.GenerateJSONFiles;
import com.natamus.collective.data.GlobalConstants;
import com.natamus.collective.events.CollectiveEvents;
import com.natamus.collective.fabric.data.GlobalFabricObjects;
import com.natamus.collective.fabric.networking.FabricNetworkHandler;
import com.natamus.collective.implementations.networking.NetworkSetup;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.util.CollectiveReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class CollectiveFabric implements ModInitializer { 
	@Override
	public void onInitialize() {
		new NetworkSetup(new FabricNetworkHandler(Side.SERVER));

		setGlobalConstants();
		CollectiveCommon.init();

		ServerLifecycleEvents.SERVER_STARTING.register((MinecraftServer minecraftServer) -> {
			GenerateJSONFiles.initGeneration(minecraftServer);
		});
		
		ServerTickEvents.START_WORLD_TICK.register((ServerLevel world) -> {
			CollectiveEvents.onWorldTick(world);
		});

		ServerTickEvents.START_SERVER_TICK.register((MinecraftServer minecraftServer) -> {
			CollectiveEvents.onServerTick(minecraftServer);
		});
		
		ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerLevel world) -> {
			CollectiveEvents.onEntityJoinLevel(world, entity);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CommandCollective.register(dispatcher);
		});

		RegisterMod.register(CollectiveReference.NAME, CollectiveReference.MOD_ID, CollectiveReference.VERSION, CollectiveReference.ACCEPTED_VERSIONS);
	}

	private static void setGlobalConstants() {
		GlobalConstants.isClient = GlobalFabricObjects.fabricLoader.getEnvironmentType() == EnvType.CLIENT;
		GlobalConstants.gameDir = GlobalFabricObjects.fabricLoader.getGameDir().toString();
	}
}
