package com.natamus.collective.networking.packets;

import com.natamus.collective.services.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EntityDataSyncClientHandler {
    public static void apply(int entityId, CompoundTag data) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        Entity entity = level.getEntity(entityId);
        if (entity != null) {
            Services.ENTITYDATA.setStored(entity, data);
        }
    }
}
