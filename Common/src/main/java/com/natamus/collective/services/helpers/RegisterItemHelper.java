package com.natamus.collective.services.helpers;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public interface RegisterItemHelper {
    default <T extends Item> void registerItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey) {
        registerItem(modEventBusObject, resourceLocation, itemFunction, properties, creativeModeTabResourceKey, false);
    }
	<T extends Item> void registerItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastItem);

	Item getRegisteredItem(ResourceLocation resourceLocation);
}