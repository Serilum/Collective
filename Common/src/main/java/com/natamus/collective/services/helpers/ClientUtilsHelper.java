package com.natamus.collective.services.helpers;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public interface ClientUtilsHelper {
	void blockSetRenderType(Block block, RenderType renderType);
	void blockSetRenderCutout(Block block);
}