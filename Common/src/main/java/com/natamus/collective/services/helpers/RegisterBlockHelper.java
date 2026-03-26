package com.natamus.collective.services.helpers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public interface RegisterBlockHelper {
    default <T extends Block> void registerBlockWithoutItem(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties) {
        registerBlockWithoutItem(modEventBusObject, Identifier, blockFunction, properties, false);
    }
	<T extends Block> void registerBlockWithoutItem(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, boolean lastBlock);
	Block getRegisteredBlockWithoutItem(Identifier Identifier);

	default <T extends Block> void registerBlockWithItem(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey) {
        registerBlockWithItem(modEventBusObject, Identifier, blockFunction, properties, creativeModeTabResourceKey, false);
    }
	<T extends Block> void registerBlockWithItem(Object modEventBusObject, Identifier Identifier, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock);
	Block getRegisteredBlockWithItem(Identifier Identifier);

	Pair<Block, BlockItem> getRegisteredBlockWithItemPair(Identifier Identifier);
	void setRegisteredBlockWithItemPair(Identifier Identifier, Class<?> blockClass, String blockFieldName, Class<?> blockItemClass, String blockItemFieldName);
}