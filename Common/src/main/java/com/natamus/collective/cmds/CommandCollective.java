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
					MessageFunctions.sendTranslatableMessage(player, "collective.collective.message.headcachereset", ChatFormatting.DARK_GREEN);
				}

				return 1;
			})))
		);
	}

	private static int showCommandHelp(CommandSourceStack source) {
		if (source.hasPermission(2)) {
			MessageFunctions.sendTranslatableMessage(source, "collective.shared.message.adminusage", true, ChatFormatting.GOLD, CollectiveReference.NAME);
			MessageFunctions.sendMessage(source, " /collective reset headcache", ChatFormatting.DARK_GREEN);
			MessageFunctions.sendTranslatableMessage(source, "     ", "collective.collective.message.headcachehelp", ChatFormatting.GRAY);
		}
		return 1;
	}
}
