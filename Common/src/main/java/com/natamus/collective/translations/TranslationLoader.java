package com.natamus.collective.translations;

import com.natamus.collective.config.CollectiveConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TranslationLoader {
	private static String lastLocale = null;

	public static void onLanguageChanged(String locale) {
		if (!CollectiveConfigHandler.downloadNonEnglishTranslations) {
			return;
		}

		if (locale == null || locale.equals(lastLocale)) {
			return;
		}

		lastLocale = locale;

		if (locale.equalsIgnoreCase("en_us")) {
			return;
		}

		new Thread(() -> {
			if (TranslationDownloader.download(locale)) {
				Minecraft.getInstance().execute(() -> inject(locale));
			}
		}, "Serilum-Translations").start();
	}

	private static void inject(String locale) {
		try {
			Path langFile = TranslationPack.getPackDir().resolve("assets").resolve("collective").resolve("lang").resolve(locale + ".json");
			if (!Files.isRegularFile(langFile)) {
				return;
			}

			Map<String, String> translations = new HashMap<>();
			try (InputStream in = Files.newInputStream(langFile)) {
				Language.loadFromJson(in, translations::put);
			}

			if (Language.getInstance() instanceof TranslationStorage storage) {
				storage.collective$mergeTranslations(translations);
			}
		}
		catch (Exception ignored) { }
	}
}
