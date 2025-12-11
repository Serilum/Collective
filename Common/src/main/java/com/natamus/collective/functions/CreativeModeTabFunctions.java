package com.natamus.collective.functions;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Nullable;

public class CreativeModeTabFunctions {
	public static @Nullable ResourceKey<CreativeModeTab> getCreativeModeTabResourceKey(String path) {
		return getCreativeModeTabResourceKey("minecraft", path);
	}
	public static @Nullable ResourceKey<CreativeModeTab> getCreativeModeTabResourceKey(String namespace, String path) {
		return getCreativeModeTabResourceKey(Identifier.fromNamespaceAndPath(namespace, path));
	}
	public static @Nullable ResourceKey<CreativeModeTab> getCreativeModeTabResourceKey(Identifier Identifier) {
		return BuiltInRegistries.CREATIVE_MODE_TAB.get(Identifier).map(creativeModeTabReference -> BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(creativeModeTabReference.value()).orElseGet(() -> { return null; })).orElse(null);
	}
}
