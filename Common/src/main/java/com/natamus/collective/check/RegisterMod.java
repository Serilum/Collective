package com.natamus.collective.check;

import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.data.Constants;
import com.natamus.collective.services.Services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RegisterMod {
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	private static final ExecutorService UPDATE_EXECUTOR = createUpdateExecutor();
	private static final int MAX_CONCURRENT_CHECKS = 10;

	public static void register(String modName, String modId, String modVersion, String rawGameVersion) {
		if (!CollectiveConfigHandler.enableUpdateChecker) {
			return;
		}

		String gameVersion = rawGameVersion.replaceAll("\\[", "").replace("]", "");
		String slug = modName.toLowerCase().replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
		String loader = Services.MODLOADER.getModLoaderName();

		UPDATE_EXECUTOR.execute(() -> checkForUpdate(slug, modName, modVersion, gameVersion, loader));
	}

	private static void checkForUpdate(String slug, String modName, String modVersion, String gameVersion, String loader) {
		try {
			String url = "https://update.serilum.com/minecraft/?mc_version=" + encode(gameVersion) + "&slug=" + encode(slug) + "&mod_version=" + encode(modVersion) + "&loader=" + encode(loader);

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(10)).GET().build();
			HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				String latestVersion = response.body().trim();
				if (isNewerVersion(latestVersion, modVersion)) {
					Constants.LOG.warn("[Update] {} has an update available: {} -> {}", modName, modVersion, latestVersion);
				}
			}
		}
		catch (Exception ignored) { }
	}

	private static boolean isNewerVersion(String latest, String current) {
		String[] latestParts = latest.split("\\.");
		String[] currentParts = current.split("\\.");
		for (int i = 0; i < Math.max(latestParts.length, currentParts.length); i++) {
			int l = i < latestParts.length ? parsePart(latestParts[i]) : 0;
			int c = i < currentParts.length ? parsePart(currentParts[i]) : 0;
			if (l != c) {
				return l > c;
			}
		}
		return false;
	}

	private static int parsePart(String part) {
		part = part.replaceAll("\\D.*", "");
		return part.isEmpty() ? 0 : Integer.parseInt(part);
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static ExecutorService createUpdateExecutor() {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(
			MAX_CONCURRENT_CHECKS, MAX_CONCURRENT_CHECKS,
			30L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			runnable -> {
				Thread thread = new Thread(runnable, "Collective Update Checker");
				thread.setDaemon(true);
				return thread;
			}
		);

		executor.allowCoreThreadTimeOut(true);
		return executor;
	}
}