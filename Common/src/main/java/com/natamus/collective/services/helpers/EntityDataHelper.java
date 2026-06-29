package com.natamus.collective.services.helpers;

import com.natamus.collective.networking.packets.EntityDataSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface EntityDataHelper {
    String KEY = "collective_entitydata";

    void init();

    CompoundTag getStored(Entity entity);

    void setStored(Entity entity, CompoundTag data);

    default int getInt(Entity entity, ResourceLocation id, int defaultValue) {
        CompoundTag data = getStored(entity);
        String key = id.toString();
        if (!data.contains(key)) {
            return defaultValue;
        }

        return data.getInt(key);
    }

    default void setInt(Entity entity, ResourceLocation id, int value) {
        CompoundTag data = getStored(entity);
        data.putInt(id.toString(), value);
        setStored(entity, data);

        if (!entity.level().isClientSide()) {
            EntityDataSyncPacket.syncToTrackers(entity);
        }
    }
}
