package com.natamus.collective.forge.services;

import com.natamus.collective.data.IEntityDataHolder;
import com.natamus.collective.networking.packets.EntityDataSyncPacket;
import com.natamus.collective.services.helpers.EntityDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ForgeEntityDataHelper implements EntityDataHelper {
    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.addListener(ForgeEntityDataHelper::onStartTracking);
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EntityDataSyncPacket.syncToPlayer(event.getTarget(), player);
        }
    }

    @Override
    public CompoundTag getStored(Entity entity) {
        // Degrade gracefully if EntityMixin's holder interface isn't applied (e.g. an outdated loader Mixin rejects it).
        if (entity instanceof IEntityDataHolder holder) {
            return holder.collective_getStored();
        }
        return new CompoundTag();
    }

    @Override
    public void setStored(Entity entity, CompoundTag data) {
        if (entity instanceof IEntityDataHolder holder) {
            holder.collective_setStored(data);
        }
    }
}
