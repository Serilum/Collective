package com.natamus.collective.services.helpers;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;

public interface RegisterItemModelPropertyHelper {
	void registerConditionalItemModelProperty(Identifier identifier, MapCodec<? extends ConditionalItemModelProperty> mapCodec);
}
