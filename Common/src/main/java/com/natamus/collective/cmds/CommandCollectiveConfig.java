package com.natamus.collective.cmds;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.config.DuskConfigEntry;
import com.natamus.collective.config.DuskConfigFile;
import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.functions.MessageFunctions;
import com.natamus.collective.globalcallbacks.ConfigCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommandCollectiveConfig {
	public static LiteralArgumentBuilder<CommandSourceStack> getCommand() {
		LiteralArgumentBuilder<CommandSourceStack> configCommand = Commands.literal("config")
			.requires((commandSourceStack) -> commandSourceStack.hasPermission(3))
			.executes((command) -> {
				return showConfigMods(command.getSource());
			});

		DuskConfigModToggles.addListOnlyMods();
		for (String modid : getSortedModIds()) {
			configCommand.then(getModCommand(DuskConfigMod.configMods.get(modid)));
		}
		return configCommand;
	}

	private static LiteralArgumentBuilder<CommandSourceStack> getModCommand(DuskConfigMod configMod) {
		LiteralArgumentBuilder<CommandSourceStack> getSubcommand = Commands.literal("get");
		LiteralArgumentBuilder<CommandSourceStack> setSubcommand = Commands.literal("set");
		LiteralArgumentBuilder<CommandSourceStack> resetSubcommand = Commands.literal("reset");
		LiteralArgumentBuilder<CommandSourceStack> addSubcommand = Commands.literal("add");
		LiteralArgumentBuilder<CommandSourceStack> removeSubcommand = Commands.literal("remove");

		boolean hasIdLists = false;
		for (DuskConfigEntry entry : configMod.getActiveEntries()) {
			String key = getCommandKey(entry);

			getSubcommand.then(Commands.literal(key)
				.executes((command) -> {
					return showValue(command.getSource(), entry);
				}));

			setSubcommand.then(Commands.literal(key)
				.then(Commands.argument("value", StringArgumentType.greedyString())
				.suggests((context, builder) -> SharedSuggestionProvider.suggest(getValueSuggestions(entry), builder))
				.executes((command) -> {
					return setValue(command.getSource(), configMod, entry, StringArgumentType.getString(command, "value"));
				})));

			resetSubcommand.then(Commands.literal(key)
				.executes((command) -> {
					return resetValue(command.getSource(), configMod, entry);
				}));

			if (entry.hasRegistry()) {
				hasIdLists = true;

				addSubcommand.then(Commands.literal(key)
					.then(Commands.argument("id", StringArgumentType.greedyString())
					.suggests((context, builder) -> SharedSuggestionProvider.suggest(getRegistryIds(context.getSource().getServer(), entry.registry), builder))
					.executes((command) -> {
						return addId(command.getSource(), configMod, entry, StringArgumentType.getString(command, "id"));
					})));

				removeSubcommand.then(Commands.literal(key)
					.then(Commands.argument("id", StringArgumentType.greedyString())
					.suggests((context, builder) -> SharedSuggestionProvider.suggest(entry.getIdList(), builder))
					.executes((command) -> {
						return removeId(command.getSource(), configMod, entry, StringArgumentType.getString(command, "id"));
					})));
			}
		}

		LiteralArgumentBuilder<CommandSourceStack> modCommand = Commands.literal(configMod.modid)
			.executes((command) -> {
				return showValues(command.getSource(), configMod);
			})
			.then(getSubcommand)
			.then(setSubcommand)
			.then(resetSubcommand)
			.then(Commands.literal("reload")
			.executes((command) -> {
				return reloadConfig(command.getSource(), configMod);
			}));

		if (hasIdLists) {
			modCommand.then(addSubcommand).then(removeSubcommand);
		}
		return modCommand;
	}

	private static int showConfigMods(CommandSourceStack source) {
		MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configmods", true, ChatFormatting.DARK_GREEN, String.join(", ", getSortedModIds()));
		return 1;
	}

	private static int showValues(CommandSourceStack source, DuskConfigMod configMod) {
		MessageFunctions.sendTranslatableMessage(source, "collective.collective.gui.configtitle", true, ChatFormatting.GOLD, configMod.name);
		for (DuskConfigEntry entry : configMod.getActiveEntries()) {
			MessageFunctions.sendMessage(source, " " + getCommandKey(entry) + ": " + entry.getText(), ChatFormatting.DARK_GREEN);
		}
		return 1;
	}

	private static int showValue(CommandSourceStack source, DuskConfigEntry entry) {
		MessageFunctions.sendTranslatableMessage(source, "options.generic_value", true, ChatFormatting.DARK_GREEN, getCommandKey(entry), entry.getText());
		showEntryInfo(source, entry);
		return 1;
	}

	private static int setValue(CommandSourceStack source, DuskConfigMod configMod, DuskConfigEntry entry, String value) {
		if (!entry.setValueFromText(value.strip())) {
			MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configinvalid", ChatFormatting.RED, value.strip());
			showEntryInfo(source, entry);
			return 0;
		}

		DuskConfig.write(configMod.modid);
		MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configset", ChatFormatting.DARK_GREEN, getCommandKey(entry), entry.getText());
		showRestartNotice(source, configMod, entry);
		return 1;
	}

	private static int resetValue(CommandSourceStack source, DuskConfigMod configMod, DuskConfigEntry entry) {
		entry.resetValue();

		DuskConfig.write(configMod.modid);
		MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configset", ChatFormatting.DARK_GREEN, getCommandKey(entry), entry.getText());
		showRestartNotice(source, configMod, entry);
		return 1;
	}

	private static int addId(CommandSourceStack source, DuskConfigMod configMod, DuskConfigEntry entry, String id) {
		String newId = id.strip();
		List<String> idList = entry.getIdList();
		if (getIdIndex(idList, newId) != -1) {
			MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configidexists", ChatFormatting.RED, newId);
			return 0;
		}

		if (entry.single) {
			if (newId.contains("*")) {
				MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configinvalid", ChatFormatting.RED, newId);
				return 0;
			}
			idList.clear();
		}

		idList.add(newId);
		entry.setIdList(idList);

		DuskConfig.write(configMod.modid);
		MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configidadded", ChatFormatting.DARK_GREEN, newId);
		return 1;
	}

	private static int removeId(CommandSourceStack source, DuskConfigMod configMod, DuskConfigEntry entry, String id) {
		String oldId = id.strip();
		List<String> idList = entry.getIdList();
		int index = getIdIndex(idList, oldId);
		if (index == -1) {
			MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configidmissing", ChatFormatting.RED, oldId);
			return 0;
		}

		idList.remove(index);
		entry.setIdList(idList);

		DuskConfig.write(configMod.modid);
		MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configidremoved", ChatFormatting.DARK_GREEN, oldId);
		return 1;
	}

	private static int reloadConfig(CommandSourceStack source, DuskConfigMod configMod) {
		if (!DuskConfigFile.reload(configMod)) {
			MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configunreadable", ChatFormatting.RED);
			return 0;
		}
		ConfigCallback.CONFIG_CHANGED.invoker().onConfigChanged(configMod.modid);

		MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configreloaded", ChatFormatting.DARK_GREEN);
		return 1;
	}

	private static void showEntryInfo(CommandSourceStack source, DuskConfigEntry entry) {
		String comment = entry.getComment();
		if (!comment.isEmpty()) {
			MessageFunctions.sendMessage(source, " " + comment, ChatFormatting.GRAY);
		}
		MessageFunctions.sendTranslatableMessage(source, " ", "collective.collective.message.configdefault", ChatFormatting.GRAY, entry.getDefaultText());
	}

	private static void showRestartNotice(CommandSourceStack source, DuskConfigMod configMod, DuskConfigEntry entry) {
		if (entry == configMod.enableEntry) {
			MessageFunctions.sendTranslatableMessage(source, "collective.collective.message.configrestart", ChatFormatting.GOLD);
		}
	}

	private static String getCommandKey(DuskConfigEntry entry) {
		return entry.getName().replace(" ", "");
	}

	private static int getIdIndex(List<String> idList, String id) {
		for (int i = 0; i < idList.size(); i++) {
			if (idList.get(i).equalsIgnoreCase(id)) {
				return i;
			}
		}
		return -1;
	}

	private static List<String> getSortedModIds() {
		List<String> modids = new ArrayList<>(DuskConfigMod.configMods.keySet());
		Collections.sort(modids);
		return modids;
	}

	private static List<String> getValueSuggestions(DuskConfigEntry entry) {
		if (entry.isBoolean()) {
			return Arrays.asList("true", "false");
		}
		if (entry.hasOptions()) {
			return entry.options;
		}
		return Arrays.asList(entry.getText());
	}

	private static List<String> getRegistryIds(MinecraftServer server, String registryName) {
		List<String> ids = new ArrayList<>();
		if (registryName.equals(DuskConfigModToggles.modRegistry)) {
			return DuskConfigModToggles.getListableModIds();
		}
		if (registryName.equals("dimension")) {
			for (ResourceKey<Level> levelKey : server.levelKeys()) {
				ids.add(levelKey.location().toString());
			}
			return ids;
		}

		ResourceLocation registryId = ResourceLocation.tryParse(registryName);
		if (registryId == null) {
			return ids;
		}

		Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryId);
		if (registry == null) {
			Optional<Registry<Object>> worldRegistry = server.registryAccess().registry(ResourceKey.createRegistryKey(registryId));
			if (!worldRegistry.isPresent()) {
				return ids;
			}
			registry = worldRegistry.get();
		}

		for (ResourceLocation resourceLocation : registry.keySet()) {
			ids.add(resourceLocation.toString());
		}
		return ids;
	}
}
