package com.natamus.collective.translations;

import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.collective.functions.WorldFunctions;
import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.collective.networking.packets.CollectiveInstalledPacket;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ServerTranslationPack {
	private static final String BASE = "https://translations.serilum.com";
	private static final UUID PACK_ID = UUID.nameUUIDFromBytes("serilum-translations".getBytes(StandardCharsets.UTF_8));
	private static final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	private static final Set<String> requiringMods = new HashSet<>();

	private static boolean itemModeDecided = false;
	private static boolean itemNamesTranslatable = true;
	private static String cachedHash = null;

	public static void requireClientTranslations(String modName) {
		requiringMods.add(modName);
	}

	public static boolean useTranslatableForMessage(ServerPlayer recipient) {
		return Dispatcher.isRegisteredOnClient(CollectiveInstalledPacket.class, recipient);
	}

	public static boolean useTranslatableForNames() {
		if (CollectiveConfigHandler.pushTranslationResourcePack) {
			return true;
		}

		String mode = CollectiveConfigHandler.itemNameTranslationMode;
		if (mode.equalsIgnoreCase("client")) {
			return true;
		}
		if (mode.equalsIgnoreCase("server")) {
			return false;
		}

		return itemNamesTranslatable;
	}

	public static void onServerStarting(MinecraftServer server) {
		itemModeDecided = false;
		itemNamesTranslatable = true;

		File file = getModeFile(server);
		if (!file.exists()) {
			return;
		}

		try {
			String content = Files.readString(file.toPath()).strip();
			itemNamesTranslatable = content.equals("translatable");
			itemModeDecided = true;
		}
		catch (Exception ignored) {
		}
	}

	public static void onPlayerJoin(ServerPlayer player) {
		decideItemMode(player);

		if (!CollectiveConfigHandler.pushTranslationResourcePack) {
			return;
		}

		if (requiringMods.isEmpty()) {
			return;
		}

		if (Dispatcher.isRegisteredOnClient(CollectiveInstalledPacket.class, player)) {
			return;
		}

		pushPack(player);
	}

	private static void decideItemMode(ServerPlayer player) {
		if (itemModeDecided) {
			return;
		}

		String mode = CollectiveConfigHandler.itemNameTranslationMode;
		if (mode.equalsIgnoreCase("client") || mode.equalsIgnoreCase("server")) {
			return;
		}

		itemNamesTranslatable = Dispatcher.isRegisteredOnClient(CollectiveInstalledPacket.class, player);
		itemModeDecided = true;

		MinecraftServer server = player.level().getServer();
		if (server != null) {
			saveItemMode(server);
		}
	}

	private static void saveItemMode(MinecraftServer server) {
		File file = getModeFile(server);

		try {
			PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
			writer.print(itemNamesTranslatable ? "translatable" : "literal");
			writer.close();
		}
		catch (Exception ignored) {
		}
	}

	private static File getModeFile(MinecraftServer server) {
		String directory = WorldFunctions.getWorldPath(server) + File.separator + "data" + File.separator + "collective";
		boolean ignored = new File(directory).mkdirs();
		return new File(directory + File.separator + "itemnametranslation.txt");
	}

	private static void pushPack(ServerPlayer player) {
		MinecraftServer server = player.level().getServer();
		if (server == null) {
			return;
		}

		String version = SharedConstants.getCurrentVersion().name();
		new Thread(() -> {
			String hash = fetchHash(version);
			if (hash == null) {
				return;
			}

			server.execute(() -> send(player, version, hash));
		}, "Serilum-Translations").start();
	}

	private static void send(ServerPlayer player, String version, String hash) {
		String url = BASE + "/pack/" + version + "/pack.zip";
		boolean required = CollectiveConfigHandler.requireTranslationResourcePack;
		player.connection.send(new ClientboundResourcePackPushPacket(PACK_ID, url, hash, required, Optional.of(buildPrompt())));
	}

	private static Component buildPrompt() {
		int count = requiringMods.size();

		String subject;
		if (count == 0) {
			subject = "Serilum mods";
		}
		else if (count >= 5) {
			subject = count + " Serilum mods";
		}
		else {
			List<String> names = new ArrayList<>(requiringMods);
			Collections.sort(names);
			subject = StringFunctions.joinListWithCommaAnd(names);
		}

		String verb = count == 1 ? "requests" : "request";
		return Component.literal(subject + " " + verb + " a resource pack to display custom item names correctly.");
	}

	private static String fetchHash(String version) {
		if (cachedHash != null) {
			return cachedHash;
		}

		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE + "/pack/" + version + "/pack.sha1")).timeout(Duration.ofSeconds(10)).GET().build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				cachedHash = response.body().trim();
				return cachedHash;
			}
		}
		catch (Exception ignored) {
		}

		return null;
	}
}
