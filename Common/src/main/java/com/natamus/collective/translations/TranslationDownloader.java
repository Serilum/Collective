package com.natamus.collective.translations;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.natamus.collective.data.Constants;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class TranslationDownloader {
	private static final String BASE = "https://translations.serilum.com";
	private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

	public static boolean download(String locale) {
		try {
			Path dir = TranslationPack.getPackDir();
			Path langFile = dir.resolve("assets").resolve("collective").resolve("lang").resolve(locale + ".json");
			Path stampFile = dir.resolve("applied.json");

			String remoteStamp = fetchStamp(locale);
			if (remoteStamp == null) {
				return false;
			}

			if (remoteStamp.equals(readStamp(stampFile, locale)) && Files.isRegularFile(langFile)) {
				return ensurePackMeta(dir);
			}

			String langText = fetch(BASE + "/lang/" + locale + ".json", 15);
			if (langText == null) {
				return false;
			}

			Files.createDirectories(langFile.getParent());
			Files.writeString(langFile, langText, StandardCharsets.UTF_8);
			ensurePackMeta(dir);
			writeStamp(stampFile, locale, remoteStamp);
			Constants.LOG.info("[Serilum Translations] Downloaded {} ({}).", locale, remoteStamp);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	private static String fetchStamp(String locale) {
		String body = fetch(BASE + "/manifest.min.json", 10);
		if (body == null) {
			return null;
		}

		JsonObject languages = JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("languages");
		return languages.has(locale) ? languages.get(locale).getAsString() : null;
	}

	private static String fetch(String url, int timeoutSeconds) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(timeoutSeconds)).GET().build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return response.statusCode() == 200 ? response.body() : null;
		}
		catch (Exception e) {
			return null;
		}
	}

	private static boolean ensurePackMeta(Path dir) throws Exception {
		PackFormat format = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES);

		JsonObject pack = new JsonObject();
		pack.addProperty("description", "Translation files for Serilum's mods.");
		pack.add("min_format", formatArray(format));
		pack.add("max_format", formatArray(format));

		JsonObject root = new JsonObject();
		root.add("pack", pack);
		String desired = root.toString();

		Path metaFile = dir.resolve("pack.mcmeta");
		if (Files.isRegularFile(metaFile) && Files.readString(metaFile).equals(desired)) {
			return false;
		}

		Files.createDirectories(dir);
		Files.writeString(metaFile, desired, StandardCharsets.UTF_8);
		return true;
	}

	private static JsonArray formatArray(PackFormat format) {
		JsonArray array = new JsonArray();
		array.add(format.major());
		array.add(format.minor());
		return array;
	}

	private static String readStamp(Path stampFile, String locale) {
		try {
			if (!Files.isRegularFile(stampFile)) {
				return "";
			}

			JsonObject stamps = JsonParser.parseString(Files.readString(stampFile)).getAsJsonObject();
			return stamps.has(locale) ? stamps.get(locale).getAsString() : "";
		}
		catch (Exception e) {
			return "";
		}
	}

	private static void writeStamp(Path stampFile, String locale, String stamp) throws Exception {
		JsonObject stamps = new JsonObject();
		if (Files.isRegularFile(stampFile)) {
			try {
				stamps = JsonParser.parseString(Files.readString(stampFile)).getAsJsonObject();
			}
			catch (Exception ignored) {
			}
		}

		stamps.addProperty(locale, stamp);
		Files.writeString(stampFile, stamps.toString(), StandardCharsets.UTF_8);
	}
}