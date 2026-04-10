package com.natamus.collective.functions;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CommandFunctions {
    public static String getRawCommandOutput(ServerLevel serverLevel, @Nullable Vec3 vec, String command) {
        BaseCommandBlock bcb = new BaseCommandBlock() {
            public @NotNull Vec3 getPosition() {
                return Objects.requireNonNullElseGet(vec, () -> new Vec3(0, 0, 0));
            }

            @Override
            public void onUpdated(@NotNull ServerLevel serverLevel) {

            }

            @Override
            public @NotNull CommandSourceStack createCommandSourceStack(@NotNull ServerLevel serverLevel, @NotNull CommandSource commandSource) {
                return new CommandSourceStack(commandSource, getPosition(), Vec2.ZERO, serverLevel, PermissionSet.ALL_PERMISSIONS, "dev", Component.literal("dev"), serverLevel.getServer(), null);
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public boolean performCommand(@NotNull ServerLevel serverLevel) {
                if ("Searge".equalsIgnoreCase(this.getCommand())) {
                    this.setLastOutput(Component.literal("#itzlipofutzli"));
                    this.setSuccessCount(1);
                }
                else {
                    this.setSuccessCount(0);
                    MinecraftServer minecraftserver = serverLevel.getServer();
                    if (!StringUtil.isNullOrEmpty(this.getCommand())) {
                        try {
                            this.setLastOutput(null);

                            try (CloseableCommandBlockSource closedCommandBlockSource = new CloseableCommandBlockSource(serverLevel)) {
                                CommandSource commandSource = (CommandSource) Objects.requireNonNullElse(closedCommandBlockSource, CommandSource.NULL);
                                CommandSourceStack commandsourcestack = this.createCommandSourceStack(serverLevel, commandSource).withCallback((b, i) -> {
                                    if (b) {
                                        this.setSuccessCount(this.getSuccessCount() + 1);
                                    }

                                });
                                minecraftserver.getCommands().performPrefixedCommand(commandsourcestack, this.getCommand());
                            }
                        }
                        catch (Throwable throwable) {
                            CrashReport crashreport = CrashReport.forThrowable(throwable, "Executing command block");
                            CrashReportCategory crashreportcategory = crashreport.addCategory("Command to be executed");
                            crashreportcategory.setDetail("Command", this::getCommand);
                            crashreportcategory.setDetail("Name", () -> this.getName().getString());
                            throw new ReportedException(crashreport);
                        }
                    }
                }
                return true;
            }
        };

        bcb.setCommand(command);
        bcb.setTrackOutput(true);
        bcb.performCommand(serverLevel);

        return bcb.getLastOutput().getString();
    }
}