package com.natamus.collective.neoforge.translations;

import com.natamus.collective.translations.TranslationPack;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class NeoForgePackFinders {
	public static void registerTranslationPack(AddPackFindersEvent event) {
		if (event.getPackType().equals(PackType.CLIENT_RESOURCES)) {
			event.addRepositorySource(TranslationPack::contribute);
		}
	}
}
