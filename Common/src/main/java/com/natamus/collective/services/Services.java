package com.natamus.collective.services;

import com.natamus.collective.services.helpers.BlockTagsHelper;
import com.natamus.collective.services.helpers.EventTriggerHelper;
import com.natamus.collective.services.helpers.ModLoaderHelper;
import com.natamus.collective.services.helpers.ToolFunctionsHelper;

import java.util.ServiceLoader;

public class Services {
    public static final BlockTagsHelper BLOCKTAGS = load(BlockTagsHelper.class);
    public static final ModLoaderHelper MODLOADER = load(ModLoaderHelper.class);
    public static final ToolFunctionsHelper TOOLFUNCTIONS = load(ToolFunctionsHelper.class);
    public static final EventTriggerHelper EVENTTRIGGER = load(EventTriggerHelper.class);

    public static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("[Collective] Failed to load service for " + clazz.getName() + "."));
    }
}