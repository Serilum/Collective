package com.natamus.collective.forge.services;

import com.mojang.serialization.MapCodec;
import com.natamus.collective.forge.mixin.ConditionalItemModelPropertiesMixin;
import com.natamus.collective.services.helpers.RegisterItemModelPropertyHelper;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;

public class ForgeRegisterItemModelPropertyHelper implements RegisterItemModelPropertyHelper {
	@Override
	public void registerConditionalItemModelProperty(Identifier identifier, MapCodec<? extends ConditionalItemModelProperty> mapCodec) {
		ConditionalItemModelPropertiesMixin.collective$getIdMapper().put(identifier, mapCodec);
	}
}
