package com.natamus.collective.functions;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import javax.annotation.Nullable;

public class CreativeModeTabFunctions {
	public static @Nullable ResourceKey<CreativeModeTab> getCreativeModeTabResourceKey(String path) {
		return getCreativeModeTabResourceKey("minecraft", path);
	}
	public static @Nullable ResourceKey<CreativeModeTab> getCreativeModeTabResourceKey(String namespace, String path) {
		return getCreativeModeTabResourceKey(ResourceLocation.fromNamespaceAndPath(namespace, path));
	}
	public static @Nullable ResourceKey<CreativeModeTab> getCreativeModeTabResourceKey(ResourceLocation resourceLocation) {
		return BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(BuiltInRegistries.CREATIVE_MODE_TAB.get(resourceLocation)).orElseGet(() -> { return null; });
	}
}
