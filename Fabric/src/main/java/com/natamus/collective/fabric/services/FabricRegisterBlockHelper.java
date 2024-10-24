package com.natamus.collective.fabric.services;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.services.helpers.RegisterBlockHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Function;

public class FabricRegisterBlockHelper implements RegisterBlockHelper {
	private static final HashMap<ResourceLocation, Block> registeredBlocksWithoutItem = new HashMap<>();
	private static final HashMap<ResourceLocation, Pair<Block, Item>> registeredBlockWithItemPairs = new HashMap<>();

    @Override
	public <T extends Block> void registerBlockWithoutItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, boolean lastBlock) {
		staticRegisterBlock(modEventBusObject, resourceLocation, blockFunction, properties, null, lastBlock, false);
    }

	@Override
	public Block getRegisteredBlockWithoutItem(ResourceLocation resourceLocation) {
		return registeredBlocksWithoutItem.get(resourceLocation);
	}

    @Override
	public <T extends Block> void registerBlockWithItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock) {
		staticRegisterBlock(modEventBusObject, resourceLocation, blockFunction, properties, creativeModeTabResourceKey, lastBlock, true);
    }

	@Override
	public Block getRegisteredBlockWithItem(ResourceLocation resourceLocation) {
		return registeredBlockWithItemPairs.get(resourceLocation).getFirst();
	}

	@Override
	public Pair<Block, BlockItem> getRegisteredBlockWithItemPair(ResourceLocation resourceLocation) {
		Pair<Block, Item> registeredPair = registeredBlockWithItemPairs.get(resourceLocation);
		return Pair.of(registeredPair.getFirst(), (BlockItem)registeredPair.getSecond());
	}

	@Override
	public void setRegisteredBlockWithItemPair(ResourceLocation resourceLocation, Class<?> blockClass, String blockFieldName, Class<?> blockItemClass, String blockItemFieldName) {
		Pair<Block, Item> registeredPair = registeredBlockWithItemPairs.get(resourceLocation);

        try {
            Field blockField = blockClass.getDeclaredField(blockFieldName);
            blockField.setAccessible(true);
            blockField.set(null, registeredPair.getFirst());

            Field blockItemField = blockItemClass.getDeclaredField(blockItemFieldName);
            blockItemField.setAccessible(true);
            blockItemField.set(null, registeredPair.getSecond());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
	}

	public static <T extends Block> void staticRegisterBlock(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock, boolean registerAsItem) {
		ResourceKey<Block> resourceKey = ResourceKey.create(Registries.BLOCK, resourceLocation);
		Block block = blockFunction.apply(properties.setId(resourceKey));

		Registry.register(BuiltInRegistries.BLOCK, resourceKey, block);

		if (registerAsItem) {
			Item item = FabricRegisterItemHelper.staticRegisterItem(modEventBusObject, resourceLocation, (itemProperties) -> new BlockItem(block, itemProperties), new Item.Properties().useBlockDescriptionPrefix(), creativeModeTabResourceKey);

			registeredBlockWithItemPairs.put(resourceLocation, Pair.of(block, item));
		}
		else {
			registeredBlocksWithoutItem.put(resourceLocation, block);
		}
	}
}