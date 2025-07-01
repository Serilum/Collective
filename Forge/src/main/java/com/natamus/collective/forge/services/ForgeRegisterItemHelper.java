package com.natamus.collective.forge.services;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.services.helpers.RegisterItemHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeRegisterItemHelper implements RegisterItemHelper {
	private static final HashMap<String, DeferredRegister<Item>> deferredItemRegisterMap = new HashMap<>();

	private static final HashMap<ResourceLocation, RegistryObject<Item>> registeredItems = new HashMap<>();

	private static final List<Pair<ResourceKey<CreativeModeTab>, RegistryObject<Item>>> creativeInventoryItemPairs = new ArrayList<>();

    @Override
	public <T extends Item> void registerItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastItem) {
		staticRegisterItem(modEventBusObject, resourceLocation, itemFunction, properties, creativeModeTabResourceKey, lastItem);
	}

    @Override
	public Item getRegisteredItem(ResourceLocation resourceLocation) {
		return registeredItems.get(resourceLocation).get();
	}

	public static <T extends Item> RegistryObject<Item> staticRegisterItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastItem) {
		String namespace = resourceLocation.getNamespace();
		if (!deferredItemRegisterMap.containsKey(namespace)) {
			DeferredRegister<Item> deferredItemRegister = DeferredRegister.create(ForgeRegistries.ITEMS, namespace);
			deferredItemRegisterMap.put(namespace, deferredItemRegister);
		}

		ResourceKey<Item> resourceKey = ResourceKey.create(Registries.ITEM, resourceLocation);
		Supplier<Item> itemSupplier = () -> itemFunction.apply(properties.setId(resourceKey));

		RegistryObject<Item> deferredItemObject = deferredItemRegisterMap.get(namespace).register(resourceLocation.getPath(), itemSupplier);

		registeredItems.put(resourceLocation, deferredItemObject);

		if (creativeModeTabResourceKey != null) {
			creativeInventoryItemPairs.add(Pair.of(creativeModeTabResourceKey, deferredItemObject));
		}

		if (lastItem) {
			deferredItemRegisterMap.get(namespace).register((BusGroup)modEventBusObject);
		}

		return deferredItemObject;
	}

	public static void addItemsToCreativeInventory(BuildCreativeModeTabContentsEvent e) {
		for (Pair<ResourceKey<CreativeModeTab>, RegistryObject<Item>> tabPair : creativeInventoryItemPairs) {
			if (e.getTabKey().equals(tabPair.getFirst())) {
				e.accept(tabPair.getSecond().get());
			}
		}
	}
}