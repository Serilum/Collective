package com.natamus.collective.fabric.services;

import com.natamus.collective.services.helpers.ClientUtilsHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;

public class FabricClientUtilsHelper implements ClientUtilsHelper {
    @Override
    public void blockSetChunkSectionLayer(Block block, ChunkSectionLayer chunkSectionLayer) {
        BlockRenderLayerMap.putBlock(block, chunkSectionLayer);
    }

    @Override
    public void blockSetRenderCutout(Block block) {
        blockSetChunkSectionLayer(block, ChunkSectionLayer.CUTOUT);
    }
}