package com.natamus.collective.globalcallbacks;

import com.natamus.collective.implementations.event.Event;
import com.natamus.collective.implementations.event.EventFactory;

public class MainMenuLoadedCallback {
	private MainMenuLoadedCallback() { }

    public static final Event<On_Main_Menu_Loaded> MAIN_MENU_LOADED = EventFactory.createArrayBacked(On_Main_Menu_Loaded.class, callbacks -> () -> {
        for (On_Main_Menu_Loaded callback : callbacks) {
        	callback.onMainMenuLoaded();
        }
    });

	@FunctionalInterface
	public interface On_Main_Menu_Loaded {
		 void onMainMenuLoaded();
	}
}
