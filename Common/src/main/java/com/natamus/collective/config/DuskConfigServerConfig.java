package com.natamus.collective.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.natamus.collective.functions.MessageFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuskConfigServerConfig {
	private static final Gson gson = new Gson();
	private static final int maxChangesLength = 4000000;
	private static final Map<UUID, StringBuilder> receivedChanges = new ConcurrentHashMap<>();

	public static String getServerConfig(ServerPlayer player, String modid) {
		JsonObject serverConfig = new JsonObject();

		DuskConfigMod configMod = getEditableConfigMod(player, modid);
		if (configMod == null) {
			serverConfig.addProperty("denied", true);
			return serverConfig.toString();
		}

		JsonObject settings = new JsonObject();
		for (DuskConfigEntry entry : configMod.entries) {
			if (entry != configMod.enableEntry) {
				settings.add(entry.getName(), getJsonValue(entry));
			}
		}

		JsonObject files = new JsonObject();
		for (DuskConfigCustomFile customFile : DuskConfigCustomFile.getCustomFiles(modid)) {
			files.addProperty(customFile.getKey(), customFile.reader.get());
		}

		serverConfig.add("settings", settings);
		serverConfig.add("files", files);
		return serverConfig.toString();
	}

	public static JsonElement getJsonValue(DuskConfigEntry entry) {
		return gson.toJsonTree(entry.getValue());
	}

	public static void useServerSettings(DuskConfigMod configMod, JsonObject settings) {
		for (DuskConfigEntry entry : configMod.entries) {
			if (settings.has(entry.getName())) {
				DuskConfigFile.readValue(entry, settings.get(entry.getName()));
			}
		}
	}

	public static void restoreOwnValues(DuskConfigMod configMod) {
		DuskConfigSync.restoreOwnValues(configMod);
		DuskConfigFile.load(configMod);
		DuskConfigSync.applyServerValues(configMod);
	}

	public static void receiveChangesPart(ServerPlayer player, String modid, String changesPart, boolean isFirstPart, boolean isLastPart) {
		if (getEditableConfigMod(player, modid) == null) {
			return;
		}

		UUID uuid = player.getUUID();
		if (isFirstPart) {
			receivedChanges.put(uuid, new StringBuilder());
		}

		StringBuilder changes = receivedChanges.get(uuid);
		if (changes == null) {
			return;
		}

		changes.append(changesPart);
		if (changes.length() > maxChangesLength) {
			receivedChanges.remove(uuid);
			return;
		}
		if (!isLastPart) {
			return;
		}

		receivedChanges.remove(uuid);
		saveServerConfig(player, modid, changes.toString());
	}

	public static void saveServerConfig(ServerPlayer player, String modid, String changesText) {
		DuskConfigMod configMod = getEditableConfigMod(player, modid);
		if (configMod == null) {
			return;
		}

		JsonObject changes;
		try {
			changes = JsonParser.parseString(changesText).getAsJsonObject();
		}
		catch (Exception ignored) {
			return;
		}

		if (changes.has("settings")) {
			saveSettings(player, configMod, changes.getAsJsonObject("settings"));
		}
		if (changes.has("files")) {
			saveFiles(player, modid, changes.getAsJsonObject("files"));
		}
	}

	private static void saveSettings(ServerPlayer player, DuskConfigMod configMod, JsonObject settings) {
		boolean changed = false;
		for (String name : settings.keySet()) {
			DuskConfigEntry entry = configMod.getEntry(name);
			if (entry == null || entry == configMod.enableEntry) {
				continue;
			}

			if (DuskConfigFile.readValue(entry, settings.get(name))) {
				changed = true;
			}
		}

		if (!changed) {
			return;
		}

		DuskConfig.write(configMod.modid);
		DuskConfigSync.sendServerValues(player.level().getServer(), configMod.modid);
		MessageFunctions.sendTranslatableMessage(player, "collective.collective.message.serverconfigsaved", ChatFormatting.DARK_GREEN, configMod.name);
	}

	private static void saveFiles(ServerPlayer player, String modid, JsonObject files) {
		for (String key : files.keySet()) {
			DuskConfigCustomFile customFile = DuskConfigCustomFile.getCustomFile(modid, key);
			if (customFile == null) {
				continue;
			}

			customFile.writer.accept(player.level().getServer(), files.get(key).getAsString());
			MessageFunctions.sendTranslatableMessage(player, "collective.collective.message.serverconfigsaved", ChatFormatting.DARK_GREEN, customFile.name);
		}
	}

	private static DuskConfigMod getEditableConfigMod(ServerPlayer player, String modid) {
		if (!player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) {
			return null;
		}

		boolean hasCustomFiles = !DuskConfigCustomFile.getCustomFiles(modid).isEmpty();

		DuskConfigMod configMod = DuskConfigMod.configMods.get(modid);
		if (configMod == null && hasCustomFiles && DuskConfigModToggles.hasConfigScreen(modid)) {
			configMod = DuskConfigMod.addListOnlyMod(modid);
		}

		if (configMod == null || configMod.isDisabled) {
			return null;
		}
		if (configMod.isListOnly && !hasCustomFiles) {
			return null;
		}
		return configMod;
	}
}
