package com.natamus.collective.translations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.natamus.collective.config.CollectiveConfigHandler;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationResolver {
	private static final String BASE = "https://translations.serilum.com";
	private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	private static final Map<String, String> translations = new ConcurrentHashMap<>();

	private static volatile boolean loaded = false;

	public static String resolve(String key, Object... args) {
		if (!loaded) {
			load();
		}

		String value = translations.get(key);
		if (value == null) {
			return key;
		}

		if (args.length == 0) {
			return value;
		}

		try {
			return String.format(value, asStrings(args));
		}
		catch (Exception ignored) {
			return value;
		}
	}

	private static Object[] asStrings(Object[] args) {
		Object[] out = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			out[i] = args[i] instanceof Component component ? component.getString() : args[i];
		}
		return out;
	}

	private static synchronized void load() {
		if (loaded) {
			return;
		}

		try (InputStream in = TranslationResolver.class.getResourceAsStream("/assets/collective/lang/en_us.json")) {
			if (in != null) {
				parseInto(new String(in.readAllBytes(), StandardCharsets.UTF_8));
			}
		}
		catch (Exception ignored) { }

		loaded = true;

		String serverLanguage = CollectiveConfigHandler.serverLanguage;
		if (serverLanguage != null && !serverLanguage.isEmpty() && !serverLanguage.equalsIgnoreCase("en_us")) {
			new Thread(() -> fetchLocale(serverLanguage), "Serilum-Translations").start();
		}
	}

	private static void fetchLocale(String locale) {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE + "/lang/" + locale + ".json")).timeout(Duration.ofSeconds(10)).GET().build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				parseInto(response.body());
			}
		}
		catch (Exception ignored) { }
	}

	private static void parseInto(String json) {
		JsonObject object = JsonParser.parseString(json).getAsJsonObject();
		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			if (!entry.getKey().startsWith("_comment_")) {
				translations.put(entry.getKey(), entry.getValue().getAsString());
			}
		}
	}
}
