package com.natamus.collective.fabric.services;

import com.mojang.serialization.MapCodec;
import com.natamus.collective.fabric.mixin.ConditionalItemModelPropertiesMixin;
import com.natamus.collective.services.helpers.RegisterItemModelPropertyHelper;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;

public class FabricRegisterItemModelPropertyHelper implements RegisterItemModelPropertyHelper {
	@Override
	public void registerConditionalItemModelProperty(Identifier identifier, MapCodec<? extends ConditionalItemModelProperty> mapCodec) {
		ConditionalItemModelPropertiesMixin.collective$getIdMapper().put(identifier, mapCodec);
	}
}
