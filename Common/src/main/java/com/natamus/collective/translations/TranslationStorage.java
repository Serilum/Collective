package com.natamus.collective.translations;

import java.util.Map;

public interface TranslationStorage {
	void collective$mergeTranslations(Map<String, String> translations);
}
