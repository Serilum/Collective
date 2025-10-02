package com.natamus.collective.services.helpers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public interface RegisterBlockHelper {
    default <T extends Block> void registerBlockWithoutItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties) {
        registerBlockWithoutItem(modEventBusObject, resourceLocation, blockFunction, properties, false);
    }
	<T extends Block> void registerBlockWithoutItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, boolean lastBlock);
	Block getRegisteredBlockWithoutItem(ResourceLocation resourceLocation);

	default <T extends Block> void registerBlockWithItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey) {
        registerBlockWithItem(modEventBusObject, resourceLocation, blockFunction, properties, creativeModeTabResourceKey, false);
    }
	<T extends Block> void registerBlockWithItem(Object modEventBusObject, ResourceLocation resourceLocation, Function<BlockBehaviour.Properties, Block> blockFunction, BlockBehaviour.Properties properties, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock);
	Block getRegisteredBlockWithItem(ResourceLocation resourceLocation);

	Pair<Block, BlockItem> getRegisteredBlockWithItemPair(ResourceLocation resourceLocation);
	void setRegisteredBlockWithItemPair(ResourceLocation resourceLocation, Class<?> blockClass, String blockFieldName, Class<?> blockItemClass, String blockItemFieldName);
}