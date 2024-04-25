package com.natamus.collective.forge.services;

import com.natamus.collective.services.helpers.TeleportHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class ForgeTeleportHelper implements TeleportHelper {
    @Override
    public <T extends Entity> Entity teleportEntity(T entity, ServerLevel serverLevel, PortalInfo portalInfo) {
        return entity.changeDimension(serverLevel, new SimpleTeleporter(portalInfo));
    }

    public record SimpleTeleporter(PortalInfo portalInfo) implements ITeleporter {
        @Override
        public PortalInfo getPortalInfo(Entity entity, ServerLevel destinationServerLevel, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
            return portalInfo;
        }
    }
}