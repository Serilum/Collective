package com.natamus.collective.functions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;

import java.util.List;

public class ExplosionFunctions {
	public static List<BlockPos> getAffectedBlockPositions(Explosion explosion) {
		return explosion.getToBlow();
	}
}
