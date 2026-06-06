package com.natamus.collective;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.forge.config.ForgeCollectiveConfigScreen;
import com.natamus.collective.forge.events.RegisterCollectiveForgeClientEvents;
import com.natamus.collective.forge.events.RegisterCollectiveForgeEvents;
import com.natamus.collective.forge.networking.ForgeNetworkHandler;
import com.natamus.collective.forge.services.ForgeRegisterItemHelper;
import com.natamus.collective.forge.services.ForgeRegisterKeyMappingHelper;
import com.natamus.collective.forge.translations.ForgePackFinders;
import com.natamus.collective.implementations.networking.NetworkSetup;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.util.CollectiveReference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(CollectiveReference.MOD_ID)
public class CollectiveForge {
	public static CollectiveForge instance;
	
    public CollectiveForge(FMLJavaModLoadingContext modLoadingContext) {
        instance = this;

        setGlobalConstants();
        CollectiveCommon.init();
        ForgeCollectiveConfigScreen.registerScreen(modLoadingContext);

        BusGroup busGroup = modLoadingContext.getModBusGroup();

		FMLCommonSetupEvent.getBus(busGroup).addListener(this::commonSetupEvent);
        FMLLoadCompleteEvent.getBus(busGroup).addListener(this::loadComplete);
		BuildCreativeModeTabContentsEvent.BUS.addListener(ForgeRegisterItemHelper::addItemsToCreativeInventory);
        RegisterKeyMappingsEvent.BUS.addListener(ForgeRegisterKeyMappingHelper::registerKeyMappings);
        AddPackFindersEvent.BUS.addListener(ForgePackFinders::registerTranslationPack);

        RegisterMod.register(CollectiveReference.NAME, CollectiveReference.MOD_ID, CollectiveReference.VERSION, CollectiveReference.ACCEPTED_VERSIONS);
    }

    private void commonSetupEvent(FMLCommonSetupEvent event) {
        new NetworkSetup(new ForgeNetworkHandler(FMLLoader.getDist().isClient() ? Side.CLIENT : Side.SERVER));
    }
	
    private void loadComplete(final FMLLoadCompleteEvent event) {
        RegisterCollectiveForgeEvents.registerEventsInBus();

        if (FMLEnvironment.dist.equals(Dist.CLIENT)) {
            RegisterCollectiveForgeClientEvents.registerEventsInBus();
        }
	}

    private static void setGlobalConstants() {

    }
}