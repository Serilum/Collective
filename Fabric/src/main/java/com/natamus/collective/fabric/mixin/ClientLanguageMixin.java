package com.natamus.collective.fabric.mixin;

import com.natamus.collective.translations.TranslationStorage;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = ClientLanguage.class, priority = 1001)
public class ClientLanguageMixin implements TranslationStorage {
	@Shadow @Final @Mutable private Map<String, String> storage;

	@Override
	public void collective$mergeTranslations(Map<String, String> translations) {
		Map<String, String> merged = new HashMap<>(this.storage);
		merged.putAll(translations);
		this.storage = merged;
	}
}
