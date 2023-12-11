package com.natamus.collective.functions;

import net.minecraft.world.damagesource.DamageSource;

public class UtilityFunctions {
    public static DamageSource createDamageSource(String identifier) {
        return new DamageSource(identifier);
    }
}
