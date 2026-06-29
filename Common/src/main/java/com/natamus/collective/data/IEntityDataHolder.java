package com.natamus.collective.data;

import net.minecraft.nbt.CompoundTag;

public interface IEntityDataHolder {
    CompoundTag collective_getStored();

    void collective_setStored(CompoundTag data);
}
