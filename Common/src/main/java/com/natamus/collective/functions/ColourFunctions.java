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

	public static int getRgbColour(String rgbString) {
		String[] channels = rgbString.split(",");
		if (channels.length != 3) {
			return -1;
		}

		int colour = 0;
		for (String rawChannel : channels) {
			int channel;
			try {
				channel = Integer.parseInt(rawChannel.strip());
			}
			catch (NumberFormatException ignored) {
				return -1;
			}

			if (channel < 0 || channel > 255) {
				return -1;
			}
			colour = (colour << 8) | channel;
		}
		return colour;
	}

	public static String getRgbString(int colour) {
		return ((colour >> 16) & 0xFF) + "," + ((colour >> 8) & 0xFF) + "," + (colour & 0xFF);
	}
}
