package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.RegisterItemHelper;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.function.Function;

public class FabricRegisterItemHelper implements RegisterItemHelper {
	private static final HashMap<Identifier, Item> itemMap = new HashMap<>();

    @Override
	public <T extends Item> void registerItem(Object modEventBusObject, Identifier Identifier, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastItem) {
		staticRegisterItem(modEventBusObject, Identifier, itemFunction, properties, creativeModeTabResourceKey);
    }

	public static <T extends Item> Item staticRegisterItem(Object modEventBusObject, Identifier Identifier, Function<Item.Properties, Item> itemFunction, Item.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey) {
		ResourceKey<Item> resourceKey = ResourceKey.create(Registries.ITEM, Identifier);
		Item item = itemFunction.apply(properties.setId(resourceKey));

		Registry.register(BuiltInRegistries.ITEM, resourceKey, item);

		if (creativeModeTabResourceKey != null) {
			CreativeModeTabEvents.modifyOutputEvent(creativeModeTabResourceKey).register(entries -> entries.accept(item));
		}

		itemMap.put(Identifier, item);

		return item;
    }

	@Override
	public Item getRegisteredItem(Identifier Identifier) {
		return itemMap.get(Identifier);
	}
}