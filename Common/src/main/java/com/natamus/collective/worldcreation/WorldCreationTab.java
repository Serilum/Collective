package com.natamus.collective.worldcreation;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public enum WorldCreationTab {
	GAME("createWorld.tab.game.title", 1),
	WORLD("createWorld.tab.world.title", 2),
	MORE("createWorld.tab.more.title", 1);

	private final String titleKey;
	public final int columns;

	WorldCreationTab(String titleKey, int columns) {
		this.titleKey = titleKey;
		this.columns = columns;
	}

	public static WorldCreationTab fromTitle(Component title) {
		if (!(title.getContents() instanceof TranslatableContents translatableContents)) {
			return null;
		}

		for (WorldCreationTab tab : values()) {
			if (tab.titleKey.equals(translatableContents.getKey())) {
				return tab;
			}
		}
		return null;
	}
}
