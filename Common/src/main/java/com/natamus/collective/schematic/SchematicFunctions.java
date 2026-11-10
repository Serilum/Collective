package com.natamus.collective.schematic;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.phys.Vec3;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class SchematicFunctions {
	public static CompoundTag readCompressed(InputStream inputStream) throws IOException {
		DataInputStream dataInputStream = createDecompressorStream(inputStream);

		CompoundTag compoundTag;
		try {
			compoundTag = NbtIo.read(dataInputStream, NbtAccounter.unlimitedHeap());
		} catch (Throwable t1) {
			if (dataInputStream != null) {
				try {
					dataInputStream.close();
				} catch (Throwable t2) {
					t1.addSuppressed(t2);
				}
			}

			throw t1;
		}

		if (dataInputStream != null) {
			dataInputStream.close();
		}

		return compoundTag;
	}

	private static DataInputStream createDecompressorStream(InputStream inputStream) throws IOException {
		return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputStream)));
	}

	public static Vec3 getPosition(ListTag doubleList) {
		return new Vec3(doubleList.getDoubleOr(0, 0), doubleList.getDoubleOr(1, 0), doubleList.getDoubleOr(2, 0));
	}

	public static ListTag getDoubleList(Vec3 position) {
		ListTag doubleList = new ListTag();
		doubleList.add(DoubleTag.valueOf(position.x));
		doubleList.add(DoubleTag.valueOf(position.y));
		doubleList.add(DoubleTag.valueOf(position.z));
		return doubleList;
	}
}
