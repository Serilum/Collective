package com.natamus.collective;

import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.data.Constants;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.util.CollectiveReference;

public class CollectiveCommon {
    public static void init() {
        Constants.LOG.info("Loading Collective version " + CollectiveReference.VERSION + ".");

        CollectiveConfigHandler.initConfig();
        GlobalVariables.generateHashMaps();
    }
}
