package com.natamus.collective.functions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;

import java.util.ArrayList;
import java.util.List;

public class ExplosionFunctions {
	public static List<BlockPos> getAffectedBlockPositions(Explosion explosion) {
		if (explosion.level().isClientSide) {
			return new ArrayList<BlockPos>();
		}
		return getAffectedBlockPositions((ServerExplosion)explosion);
	}
	public static List<BlockPos> getAffectedBlockPositions(ServerExplosion serverExplosion) {
		return serverExplosion.calculateExplodedPositions();
	}

	public static void clearExplosion(Explosion explosion) {
		if (!explosion.level().isClientSide) {
			clearExplosion((ServerExplosion)explosion);
		}
	}
	public static void clearExplosion(ServerExplosion serverExplosion) {
		serverExplosion.radius = 0;
	}
}
