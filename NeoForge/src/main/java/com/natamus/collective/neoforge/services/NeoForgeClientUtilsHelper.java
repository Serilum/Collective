package com.natamus.collective.neoforge.services;

import com.natamus.collective.services.helpers.ClientUtilsHelper;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;

public class NeoForgeClientUtilsHelper implements ClientUtilsHelper {
    @SuppressWarnings("deprecation")
	@Override
    public void blockSetChunkSectionLayer(Block block, ChunkSectionLayer chunkSectionLayer) {
        ItemBlockRenderTypes.setRenderLayer(block, chunkSectionLayer);
    }

    @Override
    public void blockSetRenderCutout(Block block) {
        blockSetChunkSectionLayer(block, ChunkSectionLayer.CUTOUT);
    }
}