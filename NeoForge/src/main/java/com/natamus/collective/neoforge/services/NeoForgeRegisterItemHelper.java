package com.natamus.collective.neoforge.services;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.services.helpers.RegisterItemHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class NeoForgeRegisterItemHelper implements RegisterItemHelper {
	private static final HashMap<String, DeferredRegister.Items> deferredItemRegisterMap = new HashMap<>();

	private static final HashMap<Identifier, DeferredItem<Item>> registeredItems = new HashMap<>();

	private static final List<Pair<ResourceKey<CreativeModeTab>, DeferredItem<Item>>> creativeInventoryItemPairs = new ArrayList<>();

    @Override
	public <T extends Item> void registerItem(Object modEventBusObject, Identifier Identifier, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastItem) {
		staticRegisterItem(modEventBusObject, Identifier, itemFunction, properties, creativeModeTabResourceKey, lastItem);
	}

    @Override
	public Item getRegisteredItem(Identifier Identifier) {
		return registeredItems.get(Identifier).get();
	}

	public static <T extends Item> DeferredItem<Item> staticRegisterItem(Object modEventBusObject, Identifier Identifier, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastItem) {
		String namespace = Identifier.getNamespace();
		if (!deferredItemRegisterMap.containsKey(namespace)) {
			DeferredRegister.Items deferredItemRegister = DeferredRegister.createItems(namespace);
			deferredItemRegisterMap.put(namespace, deferredItemRegister);
		}

		DeferredItem<Item> deferredItemObject = deferredItemRegisterMap.get(namespace).registerItem(Identifier.getPath(), itemFunction, () -> properties);

		registeredItems.put(Identifier, deferredItemObject);

		if (creativeModeTabResourceKey != null) {
			creativeInventoryItemPairs.add(Pair.of(creativeModeTabResourceKey, deferredItemObject));
		}

		if (lastItem) {
			deferredItemRegisterMap.get(namespace).register((IEventBus)modEventBusObject);
		}

		return deferredItemObject;
	}

	public static void addItemsToCreativeInventory(BuildCreativeModeTabContentsEvent e) {
		for (Pair<ResourceKey<CreativeModeTab>, DeferredItem<Item>> tabPair : creativeInventoryItemPairs) {
			if (e.getTabKey().equals(tabPair.getFirst())) {
				e.accept(tabPair.getSecond().get());
			}
		}
	}
}