package com.natamus.collective;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.forge.config.CollectiveConfigScreen;
import com.natamus.collective.forge.events.RegisterCollectiveForgeEvents;
import com.natamus.collective.util.CollectiveReference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CollectiveReference.MOD_ID)
public class CollectiveForge {
	public static CollectiveForge instance;
	
    public CollectiveForge() {
        instance = this;

        setGlobalConstants();
        CollectiveCommon.init();
        CollectiveConfigScreen.registerScreen(ModLoadingContext.get());

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    	
        modEventBus.addListener(this::loadComplete);
        
        RegisterMod.register(CollectiveReference.NAME, CollectiveReference.MOD_ID, CollectiveReference.VERSION, CollectiveReference.ACCEPTED_VERSIONS);
    }
	
    private void loadComplete(final FMLLoadCompleteEvent event) {
    	MinecraftForge.EVENT_BUS.register(new RegisterCollectiveForgeEvents());
	}

    private static void setGlobalConstants() {

    }
}