package com.natamus.collective.check;

import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.data.Constants;
import com.natamus.collective.functions.HttpFunctions;
import com.natamus.collective.services.Services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RegisterMod {
	private static final ExecutorService UPDATE_EXECUTOR = createUpdateExecutor();
	private static final int MAX_CONCURRENT_CHECKS = 10;
	private static final CountDownLatch COLLECTIVE_CONFIG_LOADED = new CountDownLatch(1);

	public static void register(String modName, String modId, String modVersion, String rawGameVersion) {
		if (COLLECTIVE_CONFIG_LOADED.getCount() == 0 && !CollectiveConfigHandler.enableUpdateChecker) {
			return;
		}

		String gameVersion = rawGameVersion.replaceAll("\\[", "").replace("]", "");
		String slug = modName.toLowerCase().replaceAll("[^a-z0-9 ]", "").replace(" ", "-");
		String loader = Services.MODLOADER.getModLoaderName();

		UPDATE_EXECUTOR.execute(() -> {
			if (!isUpdateCheckerEnabled()) {
				return;
			}

			checkForUpdate(slug, modName, modVersion, gameVersion, loader);
		});
	}

	public static void onCollectiveConfigLoaded() {
		COLLECTIVE_CONFIG_LOADED.countDown();
	}

	// Mods can register before Collective has read its own config (Forge constructs mods in parallel).
	private static boolean isUpdateCheckerEnabled() {
		try {
			if (!COLLECTIVE_CONFIG_LOADED.await(60, TimeUnit.SECONDS)) {
				return false;
			}
		}
		catch (InterruptedException ignored) {
			return false;
		}

		return CollectiveConfigHandler.enableUpdateChecker;
	}

	private static void checkForUpdate(String slug, String modName, String modVersion, String gameVersion, String loader) {
		try {
			String url = "https://update.serilum.com/minecraft/?mc_version=" + encode(gameVersion) + "&slug=" + encode(slug) + "&mod_version=" + encode(modVersion) + "&loader=" + encode(loader);

			String responseBody = HttpFunctions.getResponseBody(url, 10);
			if (responseBody != null) {
				String latestVersion = responseBody.trim();
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