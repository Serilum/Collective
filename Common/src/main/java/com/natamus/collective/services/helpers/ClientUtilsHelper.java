package com.natamus.collective.services.helpers;

import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.level.block.Block;

public interface ClientUtilsHelper {
	void blockSetChunkSectionLayer(Block block, ChunkSectionLayer chunkSectionLayer);
	void blockSetRenderCutout(Block block);
}