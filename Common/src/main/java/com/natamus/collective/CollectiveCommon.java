package com.natamus.collective;

import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.config.LoadJSONFiles;
import com.natamus.collective.data.Constants;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.networking.PacketRegistration;
import com.natamus.collective.util.CollectiveReference;

public class CollectiveCommon {
    public static void init() {
        Constants.LOG.info("Loading Collective version " + CollectiveReference.VERSION + ".");

        CollectiveConfigHandler.initConfig();
        GlobalVariables.generateHashMaps();
		LoadJSONFiles.startListening();

		registerPackets();
		loadEvents();
    }

	public static void registerPackets() {
		new PacketRegistration().init();
	}

	public static void loadEvents() {

	}
}
