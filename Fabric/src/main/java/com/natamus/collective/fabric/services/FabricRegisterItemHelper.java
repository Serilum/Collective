package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.RegisterItemHelper;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.function.Function;

public class FabricRegisterItemHelper implements RegisterItemHelper {
	private static final HashMap<ResourceLocation, Item> itemMap = new HashMap<>();

    @Override
	public <T extends Item> void registerItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastItem) {
		staticRegisterItem(modEventBusObject, resourceLocation, itemFunction, properties, creativeModeTabResourceKey);
    }

	public static <T extends Item> Item staticRegisterItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey) {
		ResourceKey<Item> resourceKey = ResourceKey.create(Registries.ITEM, resourceLocation);
		Item item = itemFunction.apply(properties.setId(resourceKey));

		Registry.register(BuiltInRegistries.ITEM, resourceKey, item);

		if (creativeModeTabResourceKey != null) {
			ItemGroupEvents.modifyEntriesEvent(creativeModeTabResourceKey).register(entries -> entries.accept(item));
		}

		itemMap.put(resourceLocation, item);

		return item;
    }

	@Override
	public Item getRegisteredItem(ResourceLocation resourceLocation) {
		return itemMap.get(resourceLocation);
	}
}