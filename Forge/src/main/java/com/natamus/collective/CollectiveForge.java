package com.natamus.collective;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.forge.config.CollectiveConfigScreen;
import com.natamus.collective.forge.events.RegisterCollectiveForgeEvents;
import com.natamus.collective.forge.networking.ForgeNetworkHandler;
import com.natamus.collective.forge.services.ForgeRegisterItemHelper;
import com.natamus.collective.implementations.networking.NetworkSetup;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.util.CollectiveReference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(CollectiveReference.MOD_ID)
public class CollectiveForge {
	public static CollectiveForge instance;
	
    public CollectiveForge() {
        instance = this;

        setGlobalConstants();
        CollectiveCommon.init();
        CollectiveConfigScreen.registerScreen(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		modEventBus.addListener(this::commonSetupEvent);
        modEventBus.addListener(this::loadComplete);
		modEventBus.addListener(ForgeRegisterItemHelper::addItemsToCreativeInventory);
        
        RegisterMod.register(CollectiveReference.NAME, CollectiveReference.MOD_ID, CollectiveReference.VERSION, CollectiveReference.ACCEPTED_VERSIONS);
    }

    private void commonSetupEvent(FMLCommonSetupEvent event) {
        new NetworkSetup(new ForgeNetworkHandler(FMLLoader.getDist().isClient() ? Side.CLIENT : Side.SERVER));
    }
	
    private void loadComplete(final FMLLoadCompleteEvent event) {
    	MinecraftForge.EVENT_BUS.register(new RegisterCollectiveForgeEvents());
	}

    private static void setGlobalConstants() {

    }
}