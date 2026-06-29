package com.natamus.collective.fabric.services;

import com.natamus.collective.data.IEntityDataHolder;
import com.natamus.collective.services.helpers.EntityDataHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class FabricEntityDataHelper implements EntityDataHelper {
    @Override
    public void init() {
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
