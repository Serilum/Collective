package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.ClientUtilsHelper;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public class FabricClientUtilsHelper implements ClientUtilsHelper {
    @Override
    public void blockSetRenderType(Block block, RenderType renderType) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, renderType);
    }

    @Override
    public void blockSetRenderCutout(Block block) {
        blockSetRenderType(block, RenderType.cutout());
    }
}