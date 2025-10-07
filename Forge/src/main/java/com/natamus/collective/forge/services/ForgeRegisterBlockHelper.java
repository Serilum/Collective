package com.natamus.collective.forge.services;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.services.helpers.RegisterBlockHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeRegisterBlockHelper implements RegisterBlockHelper {
	private static final HashMap<String, DeferredRegister<Block>> deferredBlockRegisterMap = new HashMap<>();

	private static final HashMap<ResourceLocation, RegistryObject<Block>> registeredBlocksWithoutItem = new HashMap<>();
	private static final HashMap<ResourceLocation, Pair<RegistryObject<Block>, RegistryObject<Item>>> registeredBlockWithItemPairs = new HashMap<>();

    @Override
	public <T extends Block> void registerBlockWithoutItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, boolean lastBlock) {
		staticRegisterBlock(modEventBusObject, resourceLocation, blockFunction, properties, null, lastBlock, false);
	}

	@Override
	public Block getRegisteredBlockWithoutItem(ResourceLocation resourceLocation) {
		return registeredBlocksWithoutItem.get(resourceLocation).get();
	}

    @Override
	public <T extends Block> void registerBlockWithItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock) {
		staticRegisterBlock(modEventBusObject, resourceLocation, blockFunction, properties, creativeModeTabResourceKey, lastBlock, true);
	}

	@Override
	public Block getRegisteredBlockWithItem(ResourceLocation resourceLocation) {
		return registeredBlockWithItemPairs.get(resourceLocation).getFirst().get();
	}

	@Override
	public Pair<Block, BlockItem> getRegisteredBlockWithItemPair(ResourceLocation resourceLocation) {
		Pair<RegistryObject<Block>, RegistryObject<Item>> deferredPair = registeredBlockWithItemPairs.get(resourceLocation);
		return Pair.of(deferredPair.getFirst().get(), (BlockItem)deferredPair.getSecond().get());
	}

	@Override
	public void setRegisteredBlockWithItemPair(ResourceLocation resourceLocation, Class<?> blockClass, String blockFieldName, Class<?> blockItemClass, String blockItemFieldName) {
		Pair<RegistryObject<Block>, RegistryObject<Item>> deferredPair = registeredBlockWithItemPairs.get(resourceLocation);

        try {
            Field blockField = blockClass.getDeclaredField(blockFieldName);
            blockField.setAccessible(true);
            blockField.set(null, deferredPair.getFirst().get());

            Field blockItemField = blockItemClass.getDeclaredField(blockItemFieldName);
            blockItemField.setAccessible(true);
            blockItemField.set(null, deferredPair.getSecond().get());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
	}

	public static <T extends Block> void staticRegisterBlock(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock, boolean registerAsItem) {
		String namespace = resourceLocation.getNamespace();
		if (!deferredBlockRegisterMap.containsKey(namespace)) {
			DeferredRegister<Block> deferredBlockRegister = DeferredRegister.create(ForgeRegistries.BLOCKS, namespace);
			deferredBlockRegisterMap.put(namespace, deferredBlockRegister);
		}

		ResourceKey<Block> resourceKey = ResourceKey.create(Registries.BLOCK, resourceLocation);
		Supplier<Block> blockSupplier = () -> blockFunction.apply(properties.setId(resourceKey));

		RegistryObject<Block> deferredBlockObject = deferredBlockRegisterMap.get(namespace).register(resourceLocation.getPath(), blockSupplier);

		if (registerAsItem) {
			RegistryObject<Item> deferredItemObject = ForgeRegisterItemHelper.staticRegisterItem(modEventBusObject, resourceLocation, (itemProperties) -> new BlockItem(deferredBlockObject.get(), itemProperties), new Item.Properties().useBlockDescriptionPrefix(), creativeModeTabResourceKey, lastBlock);

			registeredBlockWithItemPairs.put(resourceLocation, Pair.of(deferredBlockObject, deferredItemObject));
		}
		else {
			registeredBlocksWithoutItem.put(resourceLocation, deferredBlockObject);
		}

		if (lastBlock) {
			deferredBlockRegisterMap.get(namespace).register((BusGroup)modEventBusObject);
		}
	}
}