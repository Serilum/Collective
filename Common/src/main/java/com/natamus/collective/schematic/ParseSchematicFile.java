package com.natamus.collective.schematic;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ParseSchematicFile {
	public static ParsedSchematicObject getParsedSchematicObject(InputStream schematicInputStream, Level level, BlockPos centerPos, int extraYOffset, boolean skipAir) {
		return getParsedSchematicObject(schematicInputStream, level, centerPos, extraYOffset, skipAir, true);
	}
	public static @Nullable ParsedSchematicObject getParsedSchematicObject(InputStream schematicInputStream, Level level, BlockPos centerPos, int extraYOffset, boolean skipAir, boolean automaticCenter) {
		Schematic schematic = new Schematic(schematicInputStream, level);
		if (!schematic.wasParsedCorrectly()) {
			return null;
		}

		int maxBuildHeight = level.getMaxY();
		int length = schematic.getLength();
		int width = schematic.getWidth();
		int height = schematic.getHeight();

		int xoffset = schematic.getOffsetX();
		int yoffset = centerPos.getY() + extraYOffset;
		int zoffset = schematic.getOffsetZ();
		if (automaticCenter) {
			xoffset = -(width/2);

			if (yoffset + height > maxBuildHeight) {
				yoffset = maxBuildHeight - height;
			}

			zoffset = -(length/2);
		}
		else {
			yoffset += schematic.getOffsetY();
		}

		List<Pair<BlockPos, BlockState>> blocks = new ArrayList<>();
		for (SchematicBlockObject blockObject : schematic.getBlocks()) {
			if (blockObject == null) {
				continue;
			}

			BlockState blockState = blockObject.getState();
			if (skipAir && blockState.getBlock().equals(Blocks.AIR)) {
				continue;
			}

			blocks.add(new Pair<>(blockObject.getPosition().offset(centerPos.getX() + xoffset, yoffset, centerPos.getZ() + zoffset).immutable(), blockState));
		}

		List<Pair<BlockPos, Entity>> entities = new ArrayList<>();
		for (Pair<BlockPos, CompoundTag> rawEntityPair : schematic.getEntityRelativePosPairs()) {
			CompoundTag entityCompound = rawEntityPair.getSecond();
			BlockPos actualEntityPosition = rawEntityPair.getFirst().offset(centerPos.getX() + xoffset, yoffset, centerPos.getZ() + zoffset).immutable();
			Vec3 actualEntityExactPosition = SchematicFunctions.getPosition(entityCompound.getListOrEmpty("Pos")).add(centerPos.getX() + xoffset, yoffset, centerPos.getZ() + zoffset);

			Entity newEntity = EntityType.loadEntityRecursive(entityCompound, level, EntitySpawnReason.STRUCTURE, entity -> {
				entity.setPos(actualEntityExactPosition);
				return entity;
			});
			if (newEntity != null) {
				entities.add(new Pair<>(actualEntityPosition, newEntity));
			}
		}

		List<BlockPos> blockEntityPositions = new ArrayList<>();
		for (CompoundTag blockEntityCompoundTag : schematic.getBlockEntities()) {
			blockEntityPositions.add(schematic.getBlockPosFromCompoundTag(blockEntityCompoundTag).offset(centerPos.getX() + xoffset, yoffset, centerPos.getZ() + zoffset));
		}

		return new ParsedSchematicObject(schematic, blocks, entities, blockEntityPositions, "Parsed successfully.", true);
	}
}
