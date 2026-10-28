package com.natamus.collective;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.config.LoadJSONFiles;
import com.natamus.collective.globalcallbacks.ConfigCallback;
import com.natamus.collective.data.Constants;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.networking.PacketRegistration;
import com.natamus.collective.services.Services;
import com.natamus.collective.util.CollectiveReference;

public class CollectiveCommon {
	public static void init() {
		Constants.LOG.info("Loading Collective version " + CollectiveReference.VERSION + ".");

		DuskConfigModToggles.initCollectiveConfig();
		ConfigCallback.CONFIG_CHANGED.register(DuskConfigModToggles::onConfigChanged);

		RegisterMod.onCollectiveConfigLoaded();
		GlobalVariables.generateHashMaps();
		LoadJSONFiles.startListening();

		registerPackets();
		loadEvents();
	}

	public static void registerPackets() {
		new PacketRegistration().init();
	}

	public static void loadEvents() {
		Services.ENTITYDATA.init();
	}
}
