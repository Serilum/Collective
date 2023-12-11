package com.natamus.collective.functions;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class UtilityFunctions {
    public static DamageSource createDamageSource(String identifier) {
        DamageType damageType = new DamageType(identifier, 1F);
        return new DamageSource(Holder.direct(damageType));
    }
}
