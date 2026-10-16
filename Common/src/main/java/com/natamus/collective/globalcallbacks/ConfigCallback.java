package com.natamus.collective.globalcallbacks;

import com.natamus.collective.implementations.event.Event;
import com.natamus.collective.implementations.event.EventFactory;

public class ConfigCallback {
	private ConfigCallback() { }

	public static final Event<On_Config_Changed> CONFIG_CHANGED = EventFactory.createArrayBacked(On_Config_Changed.class, callbacks -> (modid) -> {
		for (On_Config_Changed callback : callbacks) {
			callback.onConfigChanged(modid);
		}
	});

	@FunctionalInterface
	public interface On_Config_Changed {
		 void onConfigChanged(String modid);
	}
}
