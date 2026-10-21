package com.natamus.collective.schematic;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ParsedSchematicObject {
	public Schematic schematic;
	public List<Pair<BlockPos, BlockState>> blocks;
	public List<Pair<BlockPos, Entity>> entities;
	public List<BlockPos> blockEntityPositions;
	public String parseMessageString;
	public boolean parsedCorrectly;

	public int offsetX;
	public int offsetY;
	public int offsetZ;

	public ParsedSchematicObject(Schematic _schematic, List<Pair<BlockPos, BlockState>> _blocks, List<Pair<BlockPos, Entity>> _entities, List<BlockPos> _blockEntityPositions, String _parseMessageString, boolean _parsedCorrectly) {
		schematic = _schematic;
		blocks = _blocks;
		entities = _entities;
		blockEntityPositions = _blockEntityPositions;
		parseMessageString = _parseMessageString;
		parsedCorrectly = _parsedCorrectly;

		offsetX = _schematic.getOffsetX();
		offsetY = _schematic.getOffsetY();
		offsetZ = _schematic.getOffsetZ();
	}

	public void placeBlockEntitiesInWorld(Level level) {
		for (BlockPos pos : blockEntityPositions) {
			placeBlockEntityInWorld(level, pos);
		}
	}

	public void placeBlockEntityInWorld(Level level, BlockPos pos) {
		int blockEntityIndex = blockEntityPositions.indexOf(pos);
		if (blockEntityIndex < 0) {
			return;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null) {
			return;
		}

		CompoundTag compoundTag = schematic.getBlockEntities().get(blockEntityIndex);
		blockEntity.load(compoundTag);
		blockEntity.setChanged();
	}

	public List<Pair<BlockPos, BlockEntity>> getBlockEntities(Level level) {
		List<Pair<BlockPos, BlockEntity>> blockEntities = new ArrayList<Pair<BlockPos, BlockEntity>>();
		for (BlockPos pos : blockEntityPositions) {
			blockEntities.add(new Pair<BlockPos, BlockEntity>(pos, level.getBlockEntity(pos)));
		}
		return blockEntities;
	}
}