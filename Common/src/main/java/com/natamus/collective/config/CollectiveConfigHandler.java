package com.natamus.collective.config;

import com.natamus.collective.util.CollectiveReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CollectiveConfigHandler extends DuskConfig {
    public static HashMap<String, List<String>> configMetaData = new HashMap<String, List<String>>();

    @Entry public static boolean transferItemsBetweenReplacedEntities = true;
    @Entry(min = 1, max = 500) public static int loopsAmountUsedToGetAllEntityDrops = 100;
    @Entry(min = 0, max = 3600000) public static int findABlockCheckAroundEntitiesDelayMs = 30000;
    @Entry public static boolean enableAntiRepostingCheck = true;
    @Entry public static boolean enablePatronPets = true;

    public static void initConfig() {
        configMetaData.put("transferItemsBetweenReplacedEntities", Arrays.asList(
                "When enabled, transfer the held items and armour from replaced entities by any of the Entity Spawn mods which depend on Collective."
        ));
        configMetaData.put("loopsAmountUsedToGetAllEntityDrops", Arrays.asList(
                "The amount of times Collective loops through possible mob drops to get them all procedurally. Drops are only generated when a dependent mod uses them. Lowering this can increase world load time but decrease accuracy."
        ));
        configMetaData.put("findABlockCheckAroundEntitiesDelayMs", Arrays.asList(
                "The delay of the is-there-a-block-around-check around entities in ms. Used in mods which depends on a specific blockstate in the world. Increasing this number can increase TPS if needed."
        ));
        configMetaData.put("enableAntiRepostingCheck", Arrays.asList(
                "Please check out https://stopmodreposts.org/ for more information on why this feature exists."
        ));
        configMetaData.put("enablePatronPets", Arrays.asList(
                "Enables pets for Patrons. Will be added in a future release."
        ));

        DuskConfig.init(CollectiveReference.NAME, CollectiveReference.MOD_ID, CollectiveConfigHandler.class);
    }
}