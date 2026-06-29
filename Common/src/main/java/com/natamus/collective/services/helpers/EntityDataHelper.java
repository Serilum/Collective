package com.natamus.collective.services.helpers;

import com.natamus.collective.networking.packets.EntityDataSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public interface EntityDataHelper {
    String KEY = "collective_entitydata";

    void init();

    CompoundTag getStored(Entity entity);

    void setStored(Entity entity, CompoundTag data);

    default int getInt(Entity entity, Identifier id, int defaultValue) {
        return getStored(entity).getIntOr(id.toString(), defaultValue);
    }

    default void setInt(Entity entity, Identifier id, int value) {
        CompoundTag data = getStored(entity);
        data.putInt(id.toString(), value);
        setStored(entity, data);

        if (!entity.level().isClientSide()) {
            EntityDataSyncPacket.syncToTrackers(entity);
        }
    }
}
