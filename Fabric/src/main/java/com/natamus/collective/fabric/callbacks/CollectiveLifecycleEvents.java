package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class CollectiveLifecycleEvents {
	private CollectiveLifecycleEvents() { }
	 
    public static final Event<Minecraft_Loaded> MINECRAFT_LOADED = EventFactory.createArrayBacked(Minecraft_Loaded.class, callbacks -> (isclient) -> {
        for (Minecraft_Loaded callback : callbacks) {
        	callback.onMinecraftLoad(isclient);
        }
    });

	public static final Event<Default_Language_Loaded> DEFAULT_LANGUAGE_LOADED = EventFactory.createArrayBacked(Default_Language_Loaded.class, callbacks -> () -> {
		for (Default_Language_Loaded callback : callbacks) {
			callback.onLanguageLoad();
		}
	});
    
	@FunctionalInterface
	public interface Minecraft_Loaded {
		 void onMinecraftLoad(boolean isclient);
	}

	public interface Default_Language_Loaded {
		void onLanguageLoad();
	}
}
