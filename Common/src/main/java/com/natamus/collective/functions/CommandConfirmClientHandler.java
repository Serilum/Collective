package com.natamus.collective.functions;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class CommandConfirmClientHandler {
	public static Player getClientPlayer() {
		return Minecraft.getInstance().player;
	}
}
