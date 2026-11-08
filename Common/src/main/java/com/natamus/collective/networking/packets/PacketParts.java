package com.natamus.collective.networking.packets;

import java.util.ArrayList;
import java.util.List;

public class PacketParts {
	public static List<String> split(String text, int partLength) {
		List<String> parts = new ArrayList<>();
		String rest = text;
		while (rest.length() > partLength) {
			String part = rest.substring(0, partLength);
			if (endsHalfwayThroughACharacter(part)) {
				part = part.substring(0, partLength - 1);
			}

			parts.add(part);
			rest = rest.substring(part.length());
		}

		parts.add(rest);
		return parts;
	}

	private static boolean endsHalfwayThroughACharacter(String part) {
		return Character.isHighSurrogate(part.charAt(part.length() - 1));
	}
}
