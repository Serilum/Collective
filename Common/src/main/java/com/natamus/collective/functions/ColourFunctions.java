package com.natamus.collective.functions;

import net.minecraft.ChatFormatting;

public class ColourFunctions {
	public static ChatFormatting getById(int id) {
	    if (id < 0) {
			return ChatFormatting.RESET;
	    }

	    ChatFormatting[] v = ChatFormatting.values();
	    return id < 16 ? v[id] : null;
	}
}