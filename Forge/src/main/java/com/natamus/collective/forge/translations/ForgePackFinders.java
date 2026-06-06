package com.natamus.collective.forge.translations;

import com.natamus.collective.translations.TranslationPack;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.event.AddPackFindersEvent;

public class ForgePackFinders {
	public static void registerTranslationPack(AddPackFindersEvent event) {
		if (event.getPackType().equals(PackType.CLIENT_RESOURCES)) {
			event.addRepositorySource(TranslationPack::contribute);
		}
	}
}
