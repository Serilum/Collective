package com.natamus.collective.schematic;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

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
}
