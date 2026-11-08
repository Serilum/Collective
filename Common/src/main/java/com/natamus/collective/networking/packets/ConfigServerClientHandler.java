package com.natamus.collective.networking.packets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.config.DuskConfigServerConfig;
import com.natamus.collective.config.screen.DuskConfigEditScreen;
import com.natamus.collective.functions.MessageFunctions;
import com.natamus.collective.implementations.networking.api.Dispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.permissions.Permissions;

import java.util.List;

public class ConfigServerClientHandler {
	private static final StringBuilder receivedServerConfig = new StringBuilder();
	private static Screen waitingScreen = null;
	private static Screen parentScreen = null;
	private static String requestedModid = "";
	private static DuskConfigMod editedConfigMod = null;
	private static ClientPacketListener editedConfigConnection = null;

	public static boolean canEditServerConfig() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getConnection() == null || mc.player == null || mc.isLocalServer()) {
			return false;
		}
		if (!mc.player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) {
			return false;
		}
		return Dispatcher.isRegisteredOnServer(ConfigServerRequestPacket.class);
	}

	public static void requestServerConfig(Screen parent, String modid) {
		Screen currentScreen = Minecraft.getInstance().gui.screen();
		if (isWaitingOn(currentScreen)) {
			return;
		}

		waitingScreen = currentScreen;
		parentScreen = parent;
		requestedModid = modid;
		receivedServerConfig.setLength(0);
		Dispatcher.sendToServer(new ConfigServerRequestPacket(modid));
	}

	private static boolean isWaitingOn(Screen screen) {
		return waitingScreen != null && waitingScreen == screen;
	}

	public static void receiveServerConfigPart(String modid, String serverConfigPart, boolean isLastPart) {
		if (!modid.equals(requestedModid)) {
			return;
		}

		receivedServerConfig.append(serverConfigPart);
		if (!isLastPart) {
			return;
		}

		String serverConfigText = receivedServerConfig.toString();
		receivedServerConfig.setLength(0);
		requestedModid = "";
		openServerConfig(modid, serverConfigText);
	}

	private static void openServerConfig(String modid, String serverConfigText) {
		Minecraft mc = Minecraft.getInstance();
		if (!isWaitingOn(mc.gui.screen()) || mc.player == null) {
			return;
		}

		Screen parent = parentScreen;
		waitingScreen = null;
		parentScreen = null;

		JsonObject serverConfig;
		try {
			serverConfig = JsonParser.parseString(serverConfigText).getAsJsonObject();
		}
		catch (Exception ignored) {
			return;
		}

		DuskConfigMod configMod = DuskConfigEditScreen.prepareConfigMod(modid);
		if (serverConfig.has("denied")) {
			MessageFunctions.sendClientTranslatableMessage(mc.player, "collective.collective.message.serverconfigdenied", ChatFormatting.RED);
			mc.gui.setScreen(new DuskConfigEditScreen(parent, configMod));
			return;
		}

		editedConfigMod = configMod;
		editedConfigConnection = mc.getConnection();
		mc.gui.setScreen(new DuskConfigEditScreen(parent, configMod, serverConfig));
	}

	public static void stopEditingServerConfig() {
		if (editedConfigMod == null) {
			return;
		}

		DuskConfigServerConfig.restoreOwnValues(editedConfigMod);
		editedConfigMod = null;
		editedConfigConnection = null;
	}

	public static void stopEditingServerConfigAfterLeavingServer() {
		if (Minecraft.getInstance().getConnection() != editedConfigConnection) {
			stopEditingServerConfig();
		}
	}

	public static void sendChanges(String modid, JsonObject changedSettings, JsonObject changedFiles) {
		JsonObject changes = new JsonObject();
		changes.add("settings", changedSettings);
		changes.add("files", changedFiles);

		List<String> parts = PacketParts.split(changes.toString(), ConfigServerSavePacket.partLength);
		for (int i = 0; i < parts.size(); i++) {
			boolean isFirstPart = i == 0;
			boolean isLastPart = i == parts.size() - 1;
			Dispatcher.sendToServer(new ConfigServerSavePacket(modid, parts.get(i), isFirstPart, isLastPart));
		}
	}
}
