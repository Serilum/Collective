package com.natamus.collective.functions;

import com.mojang.brigadier.context.CommandContext;
import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.services.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommandConfirmFunctions {
	private static final int confirmSeconds = 30;
	private static final int confirmsBeforeDisableHint = 3;
	private static final ConcurrentHashMap<String, Runnable> pendingActions = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Long> pendingExpiryTimes = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<UUID, Integer> sessionConfirmCounts = new ConcurrentHashMap<>();

	public static int askToConfirm(CommandContext<?> context, MutableComponent warning, Runnable action) {
		Player player = getPlayer(context);
		if (player == null || !CollectiveConfigHandler.commandsAskForConfirmation) {
			action.run();
			return 1;
		}

		String command = getCommand(context);
		String confirmationKey = getConfirmationKey(player, command);
		pendingActions.put(confirmationKey, action);
		pendingExpiryTimes.put(confirmationKey, System.currentTimeMillis() + confirmSeconds * 1000L);

		sendConfirmMessage(player, Component.literal(""));
		sendConfirmMessage(player, warning);
		sendConfirmMessage(player, getClickConfirmLine(player, command));
		sendConfirmMessage(player, getRunConfirmLine(player, command));
		return 1;
	}

	public static int confirm(CommandContext<?> context) {
		Player player = getPlayer(context);
		if (player == null) {
			return 0;
		}

		String command = getCommandWithoutConfirm(context);
		String confirmationKey = getConfirmationKey(player, command);
		Runnable action = pendingActions.remove(confirmationKey);
		Long expiresAt = pendingExpiryTimes.remove(confirmationKey);

		if (action == null || expiresAt == null || expiresAt < System.currentTimeMillis()) {
			sendConfirmMessage(player, MessageFunctions.getTranslatableMessageComponent(player, "collective.shared.message.nothingtoconfirm", "/" + command).withStyle(ChatFormatting.RED));
			return 0;
		}

		action.run();
		sendDisableHintOnThirdConfirm(player);
		return 1;
	}

	private static void sendDisableHintOnThirdConfirm(Player player) {
		int confirmCount = sessionConfirmCounts.merge(player.getUUID(), 1, Integer::sum);
		if (confirmCount != confirmsBeforeDisableHint || !hasCheatAccess(player)) {
			return;
		}

		sendConfirmMessage(player, Component.literal(""));
		sendConfirmMessage(player, MessageFunctions.getTranslatableMessageComponent(player, "collective.shared.message.confirmdisablehint").withStyle(ChatFormatting.GRAY));
	}

	private static boolean hasCheatAccess(Player player) {
		return player.hasPermissions(2);
	}

	private static Player getPlayer(CommandContext<?> context) {
		if (context.getSource() instanceof CommandSourceStack source) {
			if (source.getEntity() instanceof Player player) {
				return player;
			}
			return null;
		}

		if (Services.MODLOADER.isClientSide()) {
			return CommandConfirmClientHandler.getClientPlayer();
		}
		return null;
	}

	private static String getCommand(CommandContext<?> context) {
		String command = context.getInput().trim();
		if (command.startsWith("/")) {
			return command.substring(1);
		}
		return command;
	}

	private static String getCommandWithoutConfirm(CommandContext<?> context) {
		String command = getCommand(context);
		if (command.endsWith(" confirm")) {
			return command.substring(0, command.length() - " confirm".length());
		}
		return command;
	}

	private static String getConfirmationKey(Player player, String command) {
		return player.getUUID() + " " + command;
	}

	private static MutableComponent getClickConfirmLine(Player player, String command) {
		MutableComponent clickText = MessageFunctions.getTranslatableMessageComponent(player, "collective.shared.message.confirmclick").withStyle(ChatFormatting.GRAY);
		MutableComponent withinText = MessageFunctions.getTranslatableMessageComponent(player, "collective.shared.message.confirmwithin", confirmSeconds).withStyle(ChatFormatting.GRAY);

		return Component.literal("").append(clickText).append(" ").append(getConfirmButton(player, command)).append(" ").append(withinText);
	}

	private static MutableComponent getRunConfirmLine(Player player, String command) {
		MutableComponent orRunText = MessageFunctions.getTranslatableMessageComponent(player, "collective.shared.message.confirmorrun").withStyle(ChatFormatting.GRAY);
		MutableComponent confirmCommand = Component.literal(getConfirmCommand(command)).withStyle(ChatFormatting.WHITE);

		return Component.literal(" ").append(orRunText).append(" ").append(confirmCommand);
	}

	private static MutableComponent getConfirmButton(Player player, String command) {
		String confirmCommand = getConfirmCommand(command);

		MutableComponent confirmButton = MessageFunctions.getTranslatableMessageComponent(player, "collective.shared.message.confirmbutton").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
		confirmButton.withStyle(style -> style.withClickEvent(getConfirmClickEvent(player, confirmCommand)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(confirmCommand))));
		return confirmButton;
	}

	private static String getConfirmCommand(String command) {
		return "/" + command + " confirm";
	}

	private static ClickEvent getConfirmClickEvent(Player player, String confirmCommand) {
		if (player.level().isClientSide()) {
			// Forge does not run a clicked client command on this version, so it is put in the chat box instead.
			return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, confirmCommand);
		}
		return new ClickEvent(ClickEvent.Action.RUN_COMMAND, confirmCommand);
	}

	private static void sendConfirmMessage(Player player, MutableComponent message) {
		if (player.level().isClientSide()) {
			MessageFunctions.sendClientMessage(player, message);
		}
		else {
			MessageFunctions.sendMessage(player, message);
		}
	}
}
