package com.natamus.collective.schematic;

import com.mojang.brigadier.StringReader;
import com.mojang.datafixers.util.Pair;
import com.natamus.collective.data.Constants;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.util.*;

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
	private boolean parsedCorrectly = false;

	private static final int flatteningDataVersion = 1451;

	public Schematic(InputStream inputStream) {
		new Schematic(inputStream, null);
	}
	@SuppressWarnings("deprecation")
	public Schematic(InputStream inputStream, Level level) {
		Registry<Block> blockRegistry = BuiltInRegistries.BLOCK;
		if (level != null) {
			blockRegistry = level.registryAccess().registryOrThrow(Registries.BLOCK);
		}

		String type = "";
		try {
			CompoundTag nbtdata = NbtIo.readCompressed(inputStream);
			inputStream.close();

			if (nbtdata.contains("Schematic")) {
				nbtdata = nbtdata.getCompound("Schematic");
			}

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
			else if (nbtdata.contains("DataVersion") || nbtdata.contains("BlockData")) {
				type = "schem";
			}
			else {
				type = "schematic";
			}

			this.blockObjects = new SchematicBlockObject[size];
			this.entities = new ArrayList<>();

			switch (type) {
				case "schem" -> {
					int[] blockData = new int[width * height * length];

					byte[] blockDataRaw;
					CompoundTag palette;
					ListTag tileentitynbtlist;
					Vec3 entityPositionOrigin = Vec3.ZERO;
					boolean blockEntityDataIsNested = false;

					if (nbtdata.getInt("Version") <= 2) {
						blockDataRaw = nbtdata.getByteArray("BlockData");
						palette = nbtdata.getCompound("Palette");

						tileentitynbtlist = nbtdata.getList("BlockEntities", Tag.TAG_COMPOUND);
						if (tileentitynbtlist.isEmpty()) {
							tileentitynbtlist = nbtdata.getList("TileEntities", Tag.TAG_COMPOUND);
						}

						CompoundTag offsetCompoundTag = nbtdata.getCompound("Metadata");
						offsetX = offsetCompoundTag.getInt("WEOffsetX");
						offsetY = offsetCompoundTag.getInt("WEOffsetY");
						offsetZ = offsetCompoundTag.getInt("WEOffsetZ");

						if (nbtdata.contains("Offset")) {
							int[] minimumPoint = nbtdata.getIntArray("Offset");
							entityPositionOrigin = new Vec3(minimumPoint[0], minimumPoint[1], minimumPoint[2]);
						}
					}
					else {
						CompoundTag blocksCompoundTag = nbtdata.getCompound("Blocks");

						blockDataRaw = blocksCompoundTag.getByteArray("Data");
						palette = blocksCompoundTag.getCompound("Palette");
						tileentitynbtlist = blocksCompoundTag.getList("BlockEntities", Tag.TAG_COMPOUND);
						blockEntityDataIsNested = true;

						int[] offsetArray = nbtdata.getIntArray("Offset");

						offsetX = offsetArray[0];
						offsetY = offsetArray[1];
						offsetZ = offsetArray[2];
					}

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

					this.palette = new HashMap<>();
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

					this.blockEntities = new ArrayList<>();

					for (int t = 0; t < tileentitynbtlist.size(); t++) {
						CompoundTag blockEntityCompound = tileentitynbtlist.getCompound(t);
						if (blockEntityDataIsNested) {
							blockEntityCompound = getFlattenedBlockEntityData(blockEntityCompound);
						}
						this.blockEntities.add(blockEntityCompound);
					}

					ListTag entitiesNBTList = nbtdata.getList("Entities", Tag.TAG_COMPOUND);
					for (int entityIndex = 0; entityIndex < entitiesNBTList.size(); entityIndex++) {
						CompoundTag schemEntityCompound = entitiesNBTList.getCompound(entityIndex);

						Vec3 relativePosition = SchematicFunctions.getPosition(schemEntityCompound.getList("Pos", Tag.TAG_DOUBLE)).subtract(entityPositionOrigin);
						addEntity(BlockPos.containing(relativePosition), relativePosition, getSchemEntityData(schemEntityCompound));
					}

					parsedCorrectly = true;
					return;
				}
				case "schematic" -> {
					byte[] blockIDs_byte = nbtdata.getByteArray("Blocks");
					int[] blockIDs = new int[size];
					for (int x = 0; x < blockIDs_byte.length; x++) {
						blockIDs[x] = Byte.toUnsignedInt(blockIDs_byte[x]);
					}

					byte[] metadata = nbtdata.getByteArray("Data");
					Map<Integer, BlockState> legacyBlockStates = getLegacyBlockStates(blockRegistry, blockIDs, metadata);

					int counter = 0;
					for (int y = 0; y < height; y++) {
						for (int z = 0; z < length; z++) {
							for (int x = 0; x < width; x++) {
								BlockPos pos = new BlockPos(x, y, z);
								BlockState state = legacyBlockStates.get(getLegacyStateId(blockIDs[counter], metadata[counter]));
								blockObjects[counter] = new SchematicBlockObject(pos, state);
								counter++;
							}
						}
					}

					ListTag tileentitynbtlist = nbtdata.getList("TileEntities", Tag.TAG_COMPOUND);
					this.blockEntities = new ArrayList<>();

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

					parsedCorrectly = true;
					return;
				}
				case "nbt" -> {
					nbtdata = DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(), nbtdata, NbtUtils.getDataVersion(nbtdata, 500));
					ListTag paletteNBTList = nbtdata.getList("palette", Tag.TAG_COMPOUND);

					this.palette = new HashMap<>();
					for (int i = 0; i < paletteNBTList.size(); i++) {
						this.palette.put(i, getPaletteEntryString(paletteNBTList.getCompound(i)));
					}

					this.blockEntities = new ArrayList<>();

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

						BlockPos relativeBlockPos = new BlockPos(i0, i1, i2);
						Vec3 relativePosition = Vec3.atBottomCenterOf(relativeBlockPos);
						if (compound.contains("pos")) {
							relativePosition = SchematicFunctions.getPosition(compound.getList("pos", Tag.TAG_DOUBLE));
						}

						addEntity(relativeBlockPos, relativePosition, entityCompound);
					}

					offsetX = 0;
					offsetY = 0;
					offsetZ = 0;

					parsedCorrectly = true;
					return;
				}
			}
		} catch (Exception ignored) {
			Constants.LOG.warn("Something went wrong while parsing the schematic.");
		}

		Constants.LOG.warn("Can't load {} Schematic file.", type);
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

	private void addEntity(BlockPos relativeBlockPos, Vec3 relativePosition, CompoundTag entityCompound) {
		entityCompound.put("Pos", SchematicFunctions.getDoubleList(relativePosition));
		entityCompound.remove("UUID");
		this.entities.add(new Pair<>(relativeBlockPos, entityCompound));
	}

	private static CompoundTag getFlattenedBlockEntityData(CompoundTag schemBlockEntityCompound) {
		CompoundTag blockEntityData = schemBlockEntityCompound.getCompound("Data").copy();
		blockEntityData.putIntArray("Pos", schemBlockEntityCompound.getIntArray("Pos"));
		blockEntityData.putString("Id", schemBlockEntityCompound.getString("Id"));
		return blockEntityData;
	}

	private static CompoundTag getSchemEntityData(CompoundTag schemEntityCompound) {
		CompoundTag entityData = schemEntityCompound.copy();
		if (schemEntityCompound.contains("Data")) {
			entityData = schemEntityCompound.getCompound("Data").copy();
		}

		entityData.putString("id", schemEntityCompound.getString("Id"));
		entityData.remove("Id");
		return entityData;
	}

	public boolean isOldVersion() {
		return oldVersion;
	}

	private static int getLegacyStateId(int blockID, byte meta) {
		return (blockID << 4) | (meta & 15);
	}

	private static Map<Integer, BlockState> getLegacyBlockStates(Registry<Block> blockRegistry, int[] blockIDs, byte[] metadata) {
		Set<Integer> uniqueLegacyStateIds = new LinkedHashSet<>();
		for (int i = 0; i < blockIDs.length; i++) {
			uniqueLegacyStateIds.add(getLegacyStateId(blockIDs[i], metadata[i]));
		}
		List<Integer> legacyStateIds = new ArrayList<>(uniqueLegacyStateIds);

		ListTag flattenedPalette = new ListTag();
		for (int legacyStateId : legacyStateIds) {
			flattenedPalette.add(BlockStateData.getTag(legacyStateId).convert(NbtOps.INSTANCE).getValue());
		}

		CompoundTag flattenedStructure = new CompoundTag();
		flattenedStructure.put("palette", flattenedPalette);
		flattenedStructure.put("blocks", new ListTag());
		flattenedStructure.put("entities", new ListTag());
		ListTag currentPalette = DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(), flattenedStructure, flatteningDataVersion).getList("palette", Tag.TAG_COMPOUND);

		Map<Integer, BlockState> legacyBlockStates = new HashMap<>();
		for (int i = 0; i < legacyStateIds.size(); i++) {
			legacyBlockStates.put(legacyStateIds.get(i), getStateFromString(blockRegistry, getPaletteEntryString(currentPalette.getCompound(i))));
		}
		return legacyBlockStates;
	}

	private static String getPaletteEntryString(CompoundTag paletteEntry) {
		String nameKey = "id";
		String propertiesKey = "properties";
		if (paletteEntry.contains("Name")) {
			nameKey = "Name";
			propertiesKey = "Properties";
		}

		String value = paletteEntry.getString(nameKey);
		if (!paletteEntry.contains(propertiesKey)) {
			return value;
		}

		StringBuilder metaData = new StringBuilder("[");
		CompoundTag propertyCompound = paletteEntry.getCompound(propertiesKey);
		for (String propertyKey : propertyCompound.getAllKeys()) {
			if (!metaData.toString().equals("[")) {
				metaData.append(",");
			}

			metaData.append(propertyKey).append("=").append(propertyCompound.get(propertyKey));
		}

		metaData.append("]");
		return value + metaData;
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
		return this.size;
	}

	public SchematicBlockObject[] getBlocks() {
		return this.blockObjects;
	}

	public BlockState getStateFromID(Registry<Block> blockRegistry, int id) {
		return getStateFromString(blockRegistry, this.palette.get(id));
	}

	private static BlockState getStateFromString(Registry<Block> blockRegistry, String blockStateString) {
		try {
			return BlockStateParser.parseForBlock(blockRegistry.asLookup(), new StringReader(blockStateString), false).blockState();
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

	public boolean wasParsedCorrectly() {
		return this.parsedCorrectly;
	}
}