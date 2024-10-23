package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.RegisterItemHelper;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.function.Supplier;

public class FabricRegisterItemHelper implements RegisterItemHelper {
	private static final HashMap<ResourceLocation, Item> itemMap = new HashMap<>();

    @Override
	public <T extends Item> void registerItem(Object modEventBusObject, ResourceLocation resourceLocation, Supplier<T> itemSupplier, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastItem) {
		staticRegisterItem(modEventBusObject, resourceLocation, itemSupplier, creativeModeTabResourceKey);
    }

	public static <T extends Item> Item staticRegisterItem(Object modEventBusObject, ResourceLocation resourceLocation, Supplier<T> itemSupplier, ResourceKey<CreativeModeTab> creativeModeTabResourceKey) {
		Item item = itemSupplier.get();

		Registry.register(BuiltInRegistries.ITEM, resourceLocation, item);

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