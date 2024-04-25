package com.natamus.collective.neoforge.services;

import com.natamus.collective.services.helpers.TeleportHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.neoforged.neoforge.common.util.ITeleporter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class NeoForgeTeleportHelper implements TeleportHelper {
    @Override
    public <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, PortalInfo portalInfo) {
        return entity.changeDimension(serverLevel, new SimpleTeleporter(portalInfo));
    }

    public record SimpleTeleporter(PortalInfo portalInfo) implements ITeleporter {
        @Override
        public PortalInfo getPortalInfo(@NotNull Entity entity, @NotNull ServerLevel destinationServerLevel, @NotNull Function<ServerLevel, PortalInfo> defaultPortalInfo) {
            return portalInfo;
        }
    }
}