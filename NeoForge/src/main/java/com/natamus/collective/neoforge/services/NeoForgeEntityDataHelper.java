package com.natamus.collective.neoforge.services;

import com.natamus.collective.data.IEntityDataHolder;
import com.natamus.collective.networking.packets.EntityDataSyncPacket;
import com.natamus.collective.services.helpers.EntityDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class NeoForgeEntityDataHelper implements EntityDataHelper {
    @Override
    public void init() {
        NeoForge.EVENT_BUS.addListener(NeoForgeEntityDataHelper::onStartTracking);
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EntityDataSyncPacket.syncToPlayer(event.getTarget(), player);
        }
    }

    @Override
    public CompoundTag getStored(Entity entity) {
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
