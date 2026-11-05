package com.natamus.collective.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.collective.networking.packets.ConfigSyncPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class DuskConfigSync {
	private static final Gson gson = new Gson();
	private static final Map<String, JsonObject> serverValuesPerMod = new ConcurrentHashMap<>();
	private static final Map<DuskConfigEntry, Object> ownValues = new ConcurrentHashMap<>();
	private static final Map<DuskConfigEntry, Object> appliedServerValues = new ConcurrentHashMap<>();

	public static void sendServerValues(ServerPlayer player) {
		for (DuskConfigMod configMod : DuskConfigMod.configMods.values()) {
			sendServerValues(player, configMod);
		}
	}

	public static void sendServerValues(MinecraftServer server, String modid) {
		DuskConfigMod configMod = DuskConfigMod.configMods.get(modid);
		if (configMod == null) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			sendServerValues(player, configMod);
		}
	}

	private static void sendServerValues(ServerPlayer player, DuskConfigMod configMod) {
		if (!Dispatcher.isRegisteredOnClient(ConfigSyncPacket.class, player)) {
			return;
		}

		JsonObject serverValues = getSyncedValues(configMod);
		if (serverValues.size() == 0) {
			return;
		}

		Dispatcher.sendToClient(new ConfigSyncPacket(configMod.modid, serverValues.toString()), player);
	}

	private static JsonObject getSyncedValues(DuskConfigMod configMod) {
		JsonObject syncedValues = new JsonObject();
		for (DuskConfigEntry entry : configMod.entries) {
			if (entry.sync) {
				syncedValues.add(entry.getName(), gson.toJsonTree(getValueToSave(entry)));
			}
		}
		return syncedValues;
	}

	public static void useServerValues(String modid, String serverValues) {
		DuskConfigMod configMod = DuskConfigMod.configMods.get(modid);
		if (configMod == null) {
			return;
		}

		restoreOwnValues(configMod);
		serverValuesPerMod.put(modid, JsonParser.parseString(serverValues).getAsJsonObject());
		applyServerValues(configMod);
	}

	public static void applyServerValues(DuskConfigMod configMod) {
		JsonObject serverValues = serverValuesPerMod.get(configMod.modid);
		if (serverValues == null) {
			return;
		}

		for (DuskConfigEntry entry : configMod.entries) {
			if (!entry.sync || !serverValues.has(entry.getName())) {
				continue;
			}

			ownValues.put(entry, entry.getValue());
			DuskConfigFile.readValue(entry, serverValues.get(entry.getName()));
			appliedServerValues.put(entry, entry.getValue());
		}
	}

	public static void keepEditedValuesAsOwn(DuskConfigMod configMod) {
		for (DuskConfigEntry entry : configMod.entries) {
			if (!appliedServerValues.containsKey(entry)) {
				continue;
			}

			Object serverValue = appliedServerValues.get(entry);
			if (Objects.equals(entry.getValue(), serverValue)) {
				continue;
			}

			ownValues.put(entry, entry.getValue());
			entry.setValue(serverValue);
		}
	}

	public static boolean isUsingServerValues() {
		return !serverValuesPerMod.isEmpty();
	}

	public static void restoreOwnValues() {
		for (DuskConfigMod configMod : DuskConfigMod.configMods.values()) {
			restoreOwnValues(configMod);
		}
		serverValuesPerMod.clear();
	}

	public static void restoreOwnValues(DuskConfigMod configMod) {
		for (DuskConfigEntry entry : configMod.entries) {
			if (ownValues.containsKey(entry)) {
				entry.setValue(ownValues.remove(entry));
				appliedServerValues.remove(entry);
			}
		}
	}

	public static Object getValueToSave(DuskConfigEntry entry) {
		if (ownValues.containsKey(entry)) {
			return ownValues.get(entry);
		}
		return entry.getValue();
	}
}
