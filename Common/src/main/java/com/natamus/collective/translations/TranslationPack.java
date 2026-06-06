package com.natamus.collective.translations;

import com.natamus.collective.services.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class TranslationPack {
	public static Path getPackDir() {
		return Path.of(Services.MODLOADER.getGameDirectory(), "data", "serilum", "translations");
	}

	public static void contribute(Consumer<Pack> consumer) {
		if (!Files.isRegularFile(getPackDir().resolve("pack.mcmeta"))) {
			return;
		}

		Pack pack = buildTranslationPack();
		if (pack != null) {
			consumer.accept(pack);
		}
	}

	public static Pack buildTranslationPack() {
		return Pack.readMetaAndCreate(
				"serilum_mod_translations",
				Component.literal("Serilum Mod Translations"),
				true,
				id -> new PathPackResources(id, getPackDir(), false),
				PackType.CLIENT_RESOURCES,
				Pack.Position.BOTTOM,
				PackSource.BUILT_IN
		);
	}
}