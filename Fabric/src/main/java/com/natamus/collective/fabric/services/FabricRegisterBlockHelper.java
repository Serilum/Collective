package com.natamus.collective.fabric.services;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.services.helpers.RegisterBlockHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Function;

public class FabricRegisterBlockHelper implements RegisterBlockHelper {
	private static final HashMap<Identifier, Block> registeredBlocksWithoutItem = new HashMap<>();
	private static final HashMap<Identifier, Pair<Block, Item>> registeredBlockWithItemPairs = new HashMap<>();

    @Override
	public <T extends Block> void registerBlockWithoutItem(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, boolean lastBlock) {
		staticRegisterBlock(modEventBusObject, Identifier, blockFunction, properties, null, lastBlock, false);
    }

	@Override
	public Block getRegisteredBlockWithoutItem(Identifier Identifier) {
		return registeredBlocksWithoutItem.get(Identifier);
	}

    @Override
	public <T extends Block> void registerBlockWithItem(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock) {
		staticRegisterBlock(modEventBusObject, Identifier, blockFunction, properties, creativeModeTabResourceKey, lastBlock, true);
    }

	@Override
	public Block getRegisteredBlockWithItem(Identifier Identifier) {
		return registeredBlockWithItemPairs.get(Identifier).getFirst();
	}

	@Override
	public Pair<Block, BlockItem> getRegisteredBlockWithItemPair(Identifier Identifier) {
		Pair<Block, Item> registeredPair = registeredBlockWithItemPairs.get(Identifier);
		return Pair.of(registeredPair.getFirst(), (BlockItem)registeredPair.getSecond());
	}

	@Override
	public void setRegisteredBlockWithItemPair(Identifier Identifier, Class<?> blockClass, String blockFieldName, Class<?> blockItemClass, String blockItemFieldName) {
		Pair<Block, Item> registeredPair = registeredBlockWithItemPairs.get(Identifier);

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

	public static <T extends Block> void staticRegisterBlock(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock, boolean registerAsItem) {
		ResourceKey<Block> resourceKey = ResourceKey.create(Registries.BLOCK, Identifier);
		Block block = blockFunction.apply(properties.setId(resourceKey));

		Registry.register(BuiltInRegistries.BLOCK, resourceKey, block);

		if (registerAsItem) {
			Item item = FabricRegisterItemHelper.staticRegisterItem(modEventBusObject, Identifier, (itemProperties) -> new BlockItem(block, itemProperties), new Item.Properties().useBlockDescriptionPrefix(), creativeModeTabResourceKey);

			registeredBlockWithItemPairs.put(Identifier, Pair.of(block, item));
		}
		else {
			registeredBlocksWithoutItem.put(Identifier, block);
		}
	}
}