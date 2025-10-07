package com.natamus.collective.cmds;

import com.mojang.brigadier.CommandDispatcher;
import com.natamus.collective.features.PlayerHeadCacheFeature;
import com.natamus.collective.functions.MessageFunctions;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CommandCollective {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("collective").requires((commandSourceStack) -> commandSourceStack.isPlayer())
			.executes((command) -> {
				return showCommandHelp(command.getSource());
			})
			.then(Commands.literal("help")
			.executes((command) -> {
				return showCommandHelp(command.getSource());
			}))

			.then(Commands.literal("reset")
			.then(Commands.literal("headcache")
			.executes((command) -> {
				CommandSourceStack source = command.getSource();
				Player player = source.getPlayer();

				if (PlayerHeadCacheFeature.resetPlayerHeadCache()) {
					MessageFunctions.sendMessage(player, "The player head cache has successfully been reset.", ChatFormatting.DARK_GREEN);
				}

				return 1;
			})))
		);
	}

	private static int showCommandHelp(CommandSourceStack source) {
		if (source.hasPermission(2)) {
			MessageFunctions.sendMessage(source, Component.literal(CollectiveReference.NAME + " Admin Usage:").withStyle(ChatFormatting.GOLD), true);
			MessageFunctions.sendMessage(source, " /collective reset headcache", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendMessage(source, "     Resets Collective's cached player head data, to for example update skins.", ChatFormatting.GRAY);
		}
		return 1;
	}
}
