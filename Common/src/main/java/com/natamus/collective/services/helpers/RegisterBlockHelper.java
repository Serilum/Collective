package com.natamus.collective.services.helpers;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public interface RegisterBlockHelper {
    default <T extends Block> void registerBlockWithoutItem(Object modEventBusObject, ResourceLocation resourceLocation, Supplier<T> blockSupplier) {
        registerBlockWithoutItem(modEventBusObject, resourceLocation, blockSupplier, false);
    }
	<T extends Block> void registerBlockWithoutItem(Object modEventBusObject, ResourceLocation resourceLocation, Supplier<T> blockSupplier, boolean lastBlock);
	Block getRegisteredBlockWithoutItem(ResourceLocation resourceLocation);

	default <T extends Block> void registerBlockWithItem(Object modEventBusObject, ResourceLocation resourceLocation, Supplier<T> blockSupplier, ResourceKey<CreativeModeTab> creativeModeTabResourceKey) {
        registerBlockWithItem(modEventBusObject, resourceLocation, blockSupplier, creativeModeTabResourceKey, false);
    }
	<T extends Block> void registerBlockWithItem(Object modEventBusObject, ResourceLocation resourceLocation, Supplier<T> blockSupplier, ResourceKey<CreativeModeTab> creativeModeTabResourceKey, boolean lastBlock);
	Block getRegisteredBlockWithItem(ResourceLocation resourceLocation);

	Pair<Block, BlockItem> getRegisteredBlockWithItemPair(ResourceLocation resourceLocation);
	void setRegisteredBlockWithItemPair(ResourceLocation resourceLocation, Class<?> blockClass, String blockFieldName, Class<?> blockItemClass, String blockItemFieldName);
}