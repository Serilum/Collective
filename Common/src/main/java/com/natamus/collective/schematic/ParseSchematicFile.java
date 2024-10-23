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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParseSchematicFile {
    public static ParsedSchematicObject getParsedSchematicObject(InputStream schematicInputStream, Level level, BlockPos centerPos, int extraYOffset, boolean skipAir) {
        return getParsedSchematicObject(schematicInputStream, level, centerPos, extraYOffset, skipAir, true);
    }
    public static ParsedSchematicObject getParsedSchematicObject(InputStream schematicInputStream, Level level, BlockPos centerPos, int extraYOffset, boolean skipAir, boolean automaticCenter) {
        Schematic schematic = new Schematic(schematicInputStream, level);

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

        List<Pair<BlockPos, BlockState>> blocks = new ArrayList<Pair<BlockPos, BlockState>>();
        for (SchematicBlockObject blockObject : schematic.getBlocks()) {
            if (blockObject == null) {
                continue;
            }

            BlockState blockState = blockObject.getState();
            if (skipAir && blockState.getBlock().equals(Blocks.AIR)) {
                continue;
            }

            blocks.add(new Pair<BlockPos, BlockState>(blockObject.getPosition().offset(centerPos.getX() + xoffset, yoffset, centerPos.getZ() + zoffset).immutable(), blockState));
        }

        List<Pair<BlockPos, Entity>> entities = new ArrayList<Pair<BlockPos, Entity>>();
        for (Pair<BlockPos, CompoundTag> rawEntityPair : schematic.getEntityRelativePosPairs()) {
            Optional<Entity> optionalNewEntity = EntityType.create(rawEntityPair.getSecond(), level, EntitySpawnReason.STRUCTURE);
            if (optionalNewEntity.isPresent()) {
                BlockPos actualEntityPosition = rawEntityPair.getFirst().offset(centerPos.getX() + xoffset, yoffset, centerPos.getZ() + zoffset).immutable();
                Entity newEntity = optionalNewEntity.get();
                newEntity.setPos(actualEntityPosition.getX()+0.5, actualEntityPosition.getY(), actualEntityPosition.getZ()+0.5);
                entities.add(new Pair<BlockPos, Entity>(actualEntityPosition, newEntity));
            }
        }

        List<BlockPos> blockEntityPositions = new ArrayList<BlockPos>();
        for (CompoundTag blockEntityCompoundTag : schematic.getBlockEntities()) {
            blockEntityPositions.add(schematic.getBlockPosFromCompoundTag(blockEntityCompoundTag).offset(centerPos.getX() + xoffset, yoffset, centerPos.getZ() + zoffset));
        }

        return new ParsedSchematicObject(schematic, blocks, entities, blockEntityPositions, "Parsed successfully.", true);
    }
}
