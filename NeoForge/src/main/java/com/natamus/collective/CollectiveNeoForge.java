package com.natamus.collective;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.neoforge.config.CollectiveConfigScreen;
import com.natamus.collective.neoforge.events.RegisterCollectiveNeoForgeEvents;
import com.natamus.collective.util.CollectiveReference;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CollectiveReference.MOD_ID)
public class CollectiveNeoForge {
	public static CollectiveNeoForge instance;
	
    public CollectiveNeoForge() {
        instance = this;

        setGlobalConstants();
        CollectiveCommon.init();
        CollectiveConfigScreen.registerScreen(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    	
        modEventBus.addListener(this::loadComplete);
        
        RegisterMod.register(CollectiveReference.NAME, CollectiveReference.MOD_ID, CollectiveReference.VERSION, CollectiveReference.ACCEPTED_VERSIONS);
    }
	
    private void loadComplete(final FMLLoadCompleteEvent event) {
    	NeoForge.EVENT_BUS.register(RegisterCollectiveNeoForgeEvents.class);
	}

    private static void setGlobalConstants() {

    }
}