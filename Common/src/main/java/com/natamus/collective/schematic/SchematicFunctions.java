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
    public static CompoundTag readCompressed(InputStream $$0) throws IOException {
        DataInputStream dataInputStream = createDecompressorStream($$0);

        CompoundTag var2;
        try {
            var2 = NbtIo.read(dataInputStream, NbtAccounter.unlimitedHeap());
        } catch (Throwable var5) {
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (Throwable var4) {
                    var5.addSuppressed(var4);
                }
            }

            throw var5;
        }

        if (dataInputStream != null) {
            dataInputStream.close();
        }

        return var2;
    }

    private static DataInputStream createDecompressorStream(InputStream $$0) throws IOException {
        return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream($$0)));
    }
}
