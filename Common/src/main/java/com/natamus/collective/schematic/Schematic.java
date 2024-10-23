package com.natamus.collective.schematic;

import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import com.natamus.collective.data.Constants;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("deprecated")
public class Schematic {
	private int size;
	private short width;
	private short height;
	private short length;
	private int offsetX;
	private int offsetY;
	private int offsetZ;
	private boolean oldVersion;
	private HashMap<Integer, String> palette;
	private SchematicBlockObject[] blockObjects;
	private List<CompoundTag> blockEntities;
	private List<Pair<BlockPos, CompoundTag>> entities;

	public Schematic(InputStream inputStream) {
		new Schematic(inputStream, null);
	}
	public Schematic(InputStream inputStream, Level level) {
		Registry<Block> blockRegistry = BuiltInRegistries.BLOCK;
		if (level != null) {
			blockRegistry = level.registryAccess().lookupOrThrow(Registries.BLOCK);
		}

		String type = "";
		try {
			CompoundTag nbtdata = SchematicFunctions.readCompressed(inputStream);
			inputStream.close();

			if (nbtdata.contains("Length")) {
				width = nbtdata.getShort("Width");
				height = nbtdata.getShort("Height");
				length = nbtdata.getShort("Length");
			}
			else {
				ListTag sizeList = nbtdata.getList("size", Tag.TAG_INT);
				width = (short)sizeList.getInt(0);
				height = (short)sizeList.getInt(1);
				length = (short)sizeList.getInt(2);
			}

			size = width * height * length;

			if (nbtdata.contains("entities")) {
				type = "nbt";
			}
			else if (nbtdata.contains("DataVersion")) {
				type = "schem";
			}
			else {
				type = "schematic";
			}

			this.blockObjects = new SchematicBlockObject[size];
			this.entities = new ArrayList<Pair<BlockPos, CompoundTag>>();

			switch (type) {
				case "schem" -> {
					int[] blockData = new int[width * height * length];
					byte[] blockDataRaw = nbtdata.getByteArray("BlockData");

					int index = 0;
					int i = 0;
					while (i < blockDataRaw.length) {
						int value = 0;
						int varintLength = 0;
						while (true) {
							value |= (blockDataRaw[i] & 127) << (varintLength++ * 7);
							if (varintLength > 5) {
								continue;
							}
							if ((blockDataRaw[i] & 128) != 128) {
								i++;
								break;
							}
							i++;
						}

						blockData[index] = value;
						index++;
					}

					CompoundTag palette = nbtdata.getCompound("Palette");
					this.palette = new HashMap<Integer, String>();
					for (String k : palette.getAllKeys()) {
						this.palette.put(palette.getInt(k), k);
					}

					int counter = 0;
					for (int y = 0; y < height; y++) {
						for (int z = 0; z < length; z++) {
							for (int x = 0; x < width; x++) {
								BlockPos pos = new BlockPos(x, y, z);
								int id = blockData[counter];

								if (id < 0) {
									id *= -1;
								}

								BlockState state = getStateFromID(blockRegistry, id);

								blockObjects[counter] = new SchematicBlockObject(pos, state);
								counter++;
							}
						}
					}

					ListTag tileentitynbtlist = nbtdata.getList("BlockEntities", Tag.TAG_COMPOUND);
					this.blockEntities = new ArrayList<CompoundTag>();

					for (int t = 0; t < tileentitynbtlist.size(); t++) {
						this.blockEntities.add(tileentitynbtlist.getCompound(t));
					}

					CompoundTag offsetCompoundTag = nbtdata.getCompound("Metadata");
					offsetX = offsetCompoundTag.getInt("WEOffsetX");
					offsetY = offsetCompoundTag.getInt("WEOffsetY");
					offsetZ = offsetCompoundTag.getInt("WEOffsetZ");

					return;
				}
				case "schematic" -> {
					byte[] blockIDs_byte = nbtdata.getByteArray("Blocks");
					int[] blockIDs = new int[size];
					for (int x = 0; x < blockIDs_byte.length; x++) {
						blockIDs[x] = Byte.toUnsignedInt(blockIDs_byte[x]);
					}

					byte[] metadata = nbtdata.getByteArray("Data");

					int counter = 0;
					for (int y = 0; y < height; y++) {
						for (int z = 0; z < length; z++) {
							for (int x = 0; x < width; x++) {
								BlockPos pos = new BlockPos(x, y, z);
								BlockState state = getStateFromOldIds(blockIDs[counter], metadata[counter]);
								blockObjects[counter] = new SchematicBlockObject(pos, state);
								counter++;
							}
						}
					}

					ListTag tileentitynbtlist = nbtdata.getList("TileEntities", Tag.TAG_COMPOUND);
					this.blockEntities = new ArrayList<CompoundTag>();

					for (int i = 0; i < tileentitynbtlist.size(); i++) {
						CompoundTag compound = tileentitynbtlist.getCompound(i);
						int i0 = compound.getInt("x");
						int i1 = compound.getInt("y");
						int i2 = compound.getInt("z");
						compound.putIntArray("Pos", new int[]{i0, i1, i2});
						this.blockEntities.add(compound);
					}

					offsetX = nbtdata.getInt("WEOffsetX");
					offsetY = nbtdata.getInt("WEOffsetY");
					offsetZ = nbtdata.getInt("WEOffsetZ");

					return;
				}
				case "nbt" -> {
					ListTag paletteNBTList = nbtdata.getList("palette", Tag.TAG_COMPOUND);

					this.palette = new HashMap<Integer, String>();
					for (int i = 0; i < paletteNBTList.size(); i++) {
						CompoundTag compound = paletteNBTList.getCompound(i);
						String value = compound.getString("Name");

						if (compound.contains("Properties")) {
							StringBuilder metaData = new StringBuilder("[");

							CompoundTag propertyCompound = compound.getCompound("Properties");
							for (String propertyKey : propertyCompound.getAllKeys()) {
								if (!metaData.toString().equals("[")) {
									metaData.append(",");
								}

								metaData.append(propertyKey).append("=").append(propertyCompound.get(propertyKey));
							}

							metaData.append("]");
							value += metaData;
						}

						this.palette.put(i, value);
					}

					this.blockEntities = new ArrayList<CompoundTag>();

					ListTag blocksNBTList = nbtdata.getList("blocks", Tag.TAG_COMPOUND);
					for (int i = 0; i < blocksNBTList.size(); i++) {
						CompoundTag compound = blocksNBTList.getCompound(i);

						ListTag posList = compound.getList("pos", Tag.TAG_INT);
						int i0 = posList.getInt(0);
						int i1 = posList.getInt(1);
						int i2 = posList.getInt(2);

						BlockPos pos = new BlockPos(i0, i1, i2);

						BlockState state = getStateFromID(blockRegistry, compound.getInt("state"));
						blockObjects[i] = new SchematicBlockObject(pos, state);

						if (compound.contains("nbt")) {
							CompoundTag blockEntityCompound = compound.getCompound("nbt");
							blockEntityCompound.putIntArray("Pos", new int[]{i0, i1, i2});
							blockEntityCompound.putString("Id", blockEntityCompound.getString("id"));
							blockEntityCompound.remove("id");
							this.blockEntities.add(blockEntityCompound);
						}
					}

					ListTag entitiesNBTList = nbtdata.getList("entities", Tag.TAG_COMPOUND);
					for (int i = 0; i < entitiesNBTList.size(); i++) {
						CompoundTag compound = entitiesNBTList.getCompound(i);

						CompoundTag entityCompound = compound.getCompound("nbt");

						ListTag posList = compound.getList("blockPos", Tag.TAG_INT);
						int i0 = posList.getInt(0);
						int i1 = posList.getInt(1);
						int i2 = posList.getInt(2);

						this.entities.add(new Pair<BlockPos, CompoundTag>(new BlockPos(i0, i1, i2), entityCompound));
					}

					offsetX = 0;
					offsetY = 0;
					offsetZ = 0;

					return;
				}
			}
		} catch (Exception ignored) {
			Constants.LOG.warn("[" + CollectiveReference.NAME + "] Something went wrong while parsing the schematic.");
		}

		Constants.LOG.warn("Can't load " + type + " Schematic file.");
		this.width = 0;
		this.height = 0;
		this.length = 0;
		this.offsetX = 0;
		this.offsetY = 0;
		this.offsetZ = 0;
		this.size = 0;
		this.blockObjects = null;
		this.palette = null;
		this.blockEntities = null;
	}

	public boolean isOldVersion() {
		return oldVersion;
	}

	private BlockState getStateFromOldIds(int blockID, byte meta) {
		return Block.stateById(blockID);
	}

	public BlockState getBlockState(BlockPos pos) {
		for (SchematicBlockObject schematicBlockObject : this.blockObjects) {
			if (schematicBlockObject.getPosition().equals(pos)) {
				return schematicBlockObject.getState();
			}
		}
		return Blocks.AIR.defaultBlockState();
	}

	public int getSize() {
		return size;
	}

	public SchematicBlockObject[] getBlocks() {
		return blockObjects;
	}

	public BlockState getStateFromID(Registry<Block> blockRegistry, int id) {
		String iblockstateS = this.palette.get(id);

		try {
			return BlockStateParser.parseForBlock(blockRegistry, new StringReader(iblockstateS), false).blockState();
		} catch (Exception ex) {
			return Blocks.AIR.defaultBlockState();
		}
	}

	public List<CompoundTag> getBlockEntities() {
		return this.blockEntities;
	}

	public List<Pair<BlockPos, CompoundTag>> getEntityRelativePosPairs() {
		return this.entities;
	}

	public CompoundTag getTileEntity(BlockPos pos) {
		for (CompoundTag compound : this.blockEntities) {
			int[] pos1 = compound.getIntArray("Pos");

			if (pos1[0] == pos.getX() && pos1[1] == pos.getY() && pos1[2] == pos.getZ()) {
				return compound;
			}
		}
		return null;
	}

	public BlockPos getBlockPosFromCompoundTag(CompoundTag compoundTag) {
		int[] pos = compoundTag.getIntArray("Pos");
		return new BlockPos(pos[0], pos[1], pos[2]);
	}

	public short getWidth() {
		return width;
	}
	public short getHeight() {
		return height;
	}
	public short getLength() {
		return length;
	}

	public int getOffsetX() {
		return offsetX;
	}
	public int getOffsetY() {
		return offsetY;
	}
	public int getOffsetZ() {
		return offsetZ;
	}
}