package com.natamus.collective;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.implementations.networking.NetworkSetup;
import com.natamus.collective.implementations.networking.PacketRegistrationHandler;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.neoforge.config.CollectiveConfigScreen;
import com.natamus.collective.neoforge.events.RegisterCollectiveNeoForgeEvents;
import com.natamus.collective.neoforge.networking.NeoForgeNetworkHandler;
import com.natamus.collective.neoforge.services.NeoForgeRegisterItemHelper;
import com.natamus.collective.util.CollectiveReference;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CollectiveReference.MOD_ID)
public class CollectiveNeoForge {
	public static CollectiveNeoForge instance;

	private final PacketRegistrationHandler handler;
	
    public CollectiveNeoForge(IEventBus modEventBus) {
        instance = this;

        setGlobalConstants();
        CollectiveCommon.init();
        CollectiveConfigScreen.registerScreen(ModLoadingContext.get());
    	
        modEventBus.addListener(this::commonSetupEvent);
		modEventBus.addListener(this::loadComplete);
		modEventBus.addListener(NeoForgeRegisterItemHelper::addItemsToCreativeInventory);

		handler = new NeoForgeNetworkHandler(FMLLoader.getDist().isClient() ? Side.CLIENT : Side.SERVER);
        modEventBus.register(handler);

        RegisterMod.register(CollectiveReference.NAME, CollectiveReference.MOD_ID, CollectiveReference.VERSION, CollectiveReference.ACCEPTED_VERSIONS);
    }

    public void commonSetupEvent(FMLCommonSetupEvent event) {
        new NetworkSetup(handler);
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
    	NeoForge.EVENT_BUS.register(RegisterCollectiveNeoForgeEvents.class);
	}

    private static void setGlobalConstants() {

    }
}