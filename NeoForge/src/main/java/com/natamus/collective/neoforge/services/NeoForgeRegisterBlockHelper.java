package com.natamus.collective.neoforge.services;

import com.mojang.datafixers.util.Pair;
import com.natamus.collective.services.helpers.RegisterBlockHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.Function;

public class NeoForgeRegisterBlockHelper implements RegisterBlockHelper {
	private static final HashMap<String, DeferredRegister.Blocks> deferredBlockRegisterMap = new HashMap<>();

	private static final HashMap<Identifier, DeferredBlock<Block>> registeredBlocksWithoutItem = new HashMap<>();
	private static final HashMap<Identifier, Pair<DeferredBlock<Block>, DeferredItem<Item>>> registeredBlockWithItemPairs = new HashMap<>();

    @Override
	public <T extends Block> void registerBlockWithoutItem(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, boolean lastBlock) {
		staticRegisterBlock(modEventBusObject, Identifier, blockFunction, properties, null, lastBlock, false);
	}

	@Override
	public Block getRegisteredBlockWithoutItem(Identifier Identifier) {
		return registeredBlocksWithoutItem.get(Identifier).get();
	}

    @Override
	public <T extends Block> void registerBlockWithItem(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock) {
		staticRegisterBlock(modEventBusObject, Identifier, blockFunction, properties, creativeModeTabResourceKey, lastBlock, true);
	}

	@Override
	public Block getRegisteredBlockWithItem(Identifier Identifier) {
		return registeredBlockWithItemPairs.get(Identifier).getFirst().get();
	}

	@Override
	public Pair<Block, BlockItem> getRegisteredBlockWithItemPair(Identifier Identifier) {
		Pair<DeferredBlock<Block>, DeferredItem<Item>> deferredPair = registeredBlockWithItemPairs.get(Identifier);
		return Pair.of(deferredPair.getFirst().get(), (BlockItem)deferredPair.getSecond().get());
	}

	@Override
	public void setRegisteredBlockWithItemPair(Identifier Identifier, Class<?> blockClass, String blockFieldName, Class<?> blockItemClass, String blockItemFieldName) {
		Pair<DeferredBlock<Block>, DeferredItem<Item>> deferredPair = registeredBlockWithItemPairs.get(Identifier);

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

	public static <T extends Block> void staticRegisterBlock(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock, boolean registerAsItem) {
		String namespace = Identifier.getNamespace();
		if (!deferredBlockRegisterMap.containsKey(namespace)) {
			DeferredRegister.Blocks deferredBlockRegister = DeferredRegister.createBlocks(namespace);
			deferredBlockRegisterMap.put(namespace, deferredBlockRegister);
		}

		DeferredBlock<Block> deferredBlockObject = deferredBlockRegisterMap.get(namespace).registerBlock(Identifier.getPath(), blockFunction, () -> properties);

		if (registerAsItem) {
			DeferredItem<Item> deferredItemObject = NeoForgeRegisterItemHelper.staticRegisterItem(modEventBusObject, Identifier, (itemProperties) -> new BlockItem(deferredBlockObject.get(), itemProperties), new Item.Properties().useBlockDescriptionPrefix(), creativeModeTabResourceKey, lastBlock);

			registeredBlockWithItemPairs.put(Identifier, Pair.of(deferredBlockObject, deferredItemObject));
		}
		else {
			registeredBlocksWithoutItem.put(Identifier, deferredBlockObject);
		}

		if (lastBlock) {
			deferredBlockRegisterMap.get(namespace).register((IEventBus)modEventBusObject);
		}
	}
}