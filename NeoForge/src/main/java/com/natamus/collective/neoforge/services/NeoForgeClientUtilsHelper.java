package com.natamus.collective.neoforge.services;

import com.natamus.collective.services.helpers.ClientUtilsHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public class NeoForgeClientUtilsHelper implements ClientUtilsHelper {
    @SuppressWarnings("deprecation")
	@Override
    public void blockSetRenderType(Block block, RenderType renderType) {
        ItemBlockRenderTypes.setRenderLayer(block, renderType);
    }

    @Override
    public void blockSetRenderCutout(Block block) {
        blockSetRenderType(block, RenderType.cutout());
    }
}