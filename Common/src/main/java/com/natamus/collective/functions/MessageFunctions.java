package com.natamus.collective.functions;

import com.natamus.collective.translations.ServerTranslationPack;
import com.natamus.collective.translations.TranslationResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.net.URI;
import java.net.URISyntaxException;

public class MessageFunctions {
	public static void sendMessage(CommandSourceStack source, MutableComponent message) {
		sendMessage(source, message, false);
	}
	public static void sendMessage(Player player, MutableComponent message) {
		sendMessage(player, message, false);
	}

	public static void sendMessage(CommandSourceStack source, String m, ChatFormatting colour) {
		sendMessage(source, m, colour, false);
	}
	public static void sendMessage(Player player, String m, ChatFormatting colour) {
		sendMessage(player, m, colour, false);
	}

	public static void sendMessage(CommandSourceStack source, String m, ChatFormatting colour, boolean emptyline) {
		sendMessage(source, m, colour, emptyline, "");
	}
	public static void sendMessage(Player player, String m, ChatFormatting colour, boolean emptyline) {
		sendMessage(player, m, colour, emptyline, "");
	}

	public static void sendMessage(CommandSourceStack source, String m, ChatFormatting colour, String url) {
		sendMessage(source, m, colour, false, url);
	}
	public static void sendMessage(Player player, String m, ChatFormatting colour, String url) {
		sendMessage(player, m, colour, false, url);
	}

	public static void sendMessage(CommandSourceStack source, String m, ChatFormatting colour, boolean emptyline, String url) {
		if (m.isEmpty()) {
			return;
		}

		sendMessage(source, buildMessage(m, colour, url), emptyline);
	}
	public static void sendMessage(CommandSourceStack source, MutableComponent message, boolean emptyline) {
		if (emptyline) {
			source.sendSuccess(() -> Component.literal(""), false);
		}

		source.sendSuccess(() -> message, false);
	}

	public static void sendMessage(Player player, String m, ChatFormatting colour, boolean emptyline, String url) {
		if (m.isEmpty()) {
			return;
		}

		sendMessage(player, buildMessage(m, colour, url), emptyline);
	}
	public static void sendMessage(Player player, MutableComponent message, boolean emptyline) {
		if (player.level().isClientSide()) {
			return;
		}

		ServerPlayer serverPlayer = (ServerPlayer)player;
		if (emptyline) {
			serverPlayer.sendSystemMessage(Component.literal(""));
		}

		serverPlayer.sendSystemMessage(message);
	}

	public static void sendClientMessage(Player player, MutableComponent message) {
		player.sendSystemMessage(message);
	}

	public static void sendClientTranslatableMessage(Player player, String key, ChatFormatting colour, Object... args) {
		sendClientMessage(player, withColour(translatableComponent(player, key, args), colour));
	}

	public static void sendClientTranslatableMessage(Player player, String indent, String key, ChatFormatting colour, Object... args) {
		sendClientMessage(player, withColour(Component.literal(indent).append(translatableComponent(player, key, args)), colour));
	}

	public static void sendTranslatableMessage(CommandSourceStack source, String key, ChatFormatting colour, Object... args) {
		sendMessage(source, withColour(translatableComponent(source, key, args), colour));
	}
	public static void sendTranslatableMessage(Player player, String key, ChatFormatting colour, Object... args) {
		sendMessage(player, withColour(translatableComponent(player, key, args), colour));
	}

	public static void sendTranslatableMessage(CommandSourceStack source, String key, boolean emptyLine, ChatFormatting colour, Object... args) {
		sendMessage(source, withColour(translatableComponent(source, key, args), colour), emptyLine);
	}
	public static void sendTranslatableMessage(Player player, String key, boolean emptyLine, ChatFormatting colour, Object... args) {
		sendMessage(player, withColour(translatableComponent(player, key, args), colour), emptyLine);
	}

	public static void sendTranslatableMessage(CommandSourceStack source, String indent, String key, ChatFormatting colour, Object... args) {
		sendMessage(source, withColour(Component.literal(indent).append(translatableComponent(source, key, args)), colour));
	}
	public static void sendTranslatableMessage(Player player, String indent, String key, ChatFormatting colour, Object... args) {
		sendMessage(player, withColour(Component.literal(indent).append(translatableComponent(player, key, args)), colour));
	}

	public static void sendTranslatableMessage(CommandSourceStack source, String indent, String key, boolean emptyLine, ChatFormatting colour, Object... args) {
		sendMessage(source, withColour(Component.literal(indent).append(translatableComponent(source, key, args)), colour), emptyLine);
	}
	public static void sendTranslatableMessage(Player player, String indent, String key, boolean emptyLine, ChatFormatting colour, Object... args) {
		sendMessage(player, withColour(Component.literal(indent).append(translatableComponent(player, key, args)), colour), emptyLine);
	}

	public static void sendCommandUsageHeader(CommandSourceStack source, String modName) {
		sendTranslatableMessage(source, "collective.shared.message.usage", true, ChatFormatting.DARK_GREEN, modName);
	}
	public static void sendCommandUsageLine(CommandSourceStack source, String command, String descriptionKey, Object... args) {
		sendMessage(source, " /" + command, ChatFormatting.DARK_GREEN);
		sendTranslatableMessage(source, "  ", descriptionKey, ChatFormatting.GRAY, args);
	}

	public static MutableComponent getTranslatableComponent(String key, Object... args) {
		if (ServerTranslationPack.useTranslatableForNames()) {
			return Component.translatable(key, args);
		}
		return Component.literal(TranslationResolver.resolve(key, args));
	}

	public static MutableComponent getTranslatableMessageComponent(Player player, String key, Object... args) {
		return translatableComponent(player, key, args);
	}
	public static MutableComponent getTranslatableMessageComponent(CommandSourceStack source, String key, Object... args) {
		return translatableComponent(source, key, args);
	}

	private static MutableComponent translatableComponent(Player player, String key, Object... args) {
		if (player.level().isClientSide()) {
			return Component.translatable(key, args);
		}
		if (player instanceof ServerPlayer serverPlayer && ServerTranslationPack.useTranslatableForMessage(serverPlayer)) {
			return Component.translatable(key, args);
		}
		return Component.literal(TranslationResolver.resolve(key, args));
	}
	private static MutableComponent translatableComponent(CommandSourceStack source, String key, Object... args) {
		Entity sourceEntity = source.getEntity();
		if (sourceEntity != null && sourceEntity.level().isClientSide()) {
			return Component.translatable(key, args);
		}
		ServerPlayer player = source.getPlayer();
		if (player != null && ServerTranslationPack.useTranslatableForMessage(player)) {
			return Component.translatable(key, args);
		}
		return Component.literal(TranslationResolver.resolve(key, args));
	}

	private static MutableComponent withColour(MutableComponent message, ChatFormatting colour) {
		if (colour != null) {
			message.withStyle(colour);
		}
		return message;
	}

	private static MutableComponent buildMessage(String m, ChatFormatting colour, String url) {
		if (!url.isEmpty()) {
			return withColour(withLink(Component.literal(m), url), colour);
		}
		if (!m.contains("http") && !m.contains("www.")) {
			return withColour(Component.literal(m), colour);
		}
		return withColour(withClickableLinks(m), colour);
	}

	private static MutableComponent withClickableLinks(String m) {
		MutableComponent message = Component.literal("");
		StringBuilder plainText = new StringBuilder();
		String wordSeparator = "";

		for (String word : m.split(" ", -1)) {
			plainText.append(wordSeparator);
			wordSeparator = " ";

			String link = getLinkInWord(word);
			if (link.isEmpty()) {
				plainText.append(word);
				continue;
			}

			plainText.append(StringFunctions.getTextBefore(word, link));
			message.append(plainText.toString());
			message.append(withLink(Component.literal(link), link));
			plainText = new StringBuilder(StringFunctions.getTextAfter(word, link));
		}

		message.append(plainText.toString());
		return message;
	}

	private static String getLinkInWord(String word) {
		String link = word;
		while (!link.isEmpty() && !link.startsWith("http") && !link.startsWith("www.")) {
			link = link.substring(1);
		}
		while (!link.isEmpty() && ".,;:!?)]}>'\"".indexOf(link.charAt(link.length() - 1)) >= 0) {
			link = link.substring(0, link.length() - 1);
		}
		return link;
	}

	private static MutableComponent withLink(MutableComponent component, String link) {
		String fullLink = link;
		if (link.startsWith("www.")) {
			fullLink = "https://" + link;
		}

		try {
			URI uri = Util.parseAndValidateUntrustedUri(fullLink);
			component.withStyle(component.getStyle().withClickEvent(new ClickEvent.OpenUrl(uri)));
		}
		catch (URISyntaxException ignored) { }
		return component;
	}

	public static void broadcastMessage(Level world, String m, ChatFormatting colour) {
		if (m.isEmpty()) {
			return;
		}

		broadcastMessage(world, withColour(Component.literal(m), colour));
	}
	public static void broadcastMessage(Level world, MutableComponent message) {
		MinecraftServer server = world.getServer();
		if (server == null) {
			return;
		}

		for (Player player : server.getPlayerList().getPlayers()) {
			sendMessage(player, message);
		}
	}

	public static void broadcastTranslatableMessage(Level world, String key, ChatFormatting colour, Object... args) {
		MinecraftServer server = world.getServer();
		if (server == null) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			sendMessage(player, withColour(translatableComponent(player, key, args), colour));
		}
	}

	public static void sendMessageToPlayersAround(Level world, BlockPos p, int radius, String message, ChatFormatting colour) {
		if (message.isEmpty()) {
			return;
		}

		for (Entity around : world.getEntities(null, new AABB(p.getX() - radius, p.getY() - radius, p.getZ() - radius, p.getX() + radius, p.getY() + radius, p.getZ() + radius))) {
			if (around instanceof Player) {
				sendMessage((Player) around, message, colour);
			}
		}
	}
	public static void sendMessageToPlayersAround(Level world, BlockPos p, int radius, MutableComponent message) {
		for (Entity around : world.getEntities(null, new AABB(p.getX() - radius, p.getY() - radius, p.getZ() - radius, p.getX() + radius, p.getY() + radius, p.getZ() + radius))) {
			if (around instanceof Player) {
				sendMessage((Player) around, message);
			}
		}
	}
}