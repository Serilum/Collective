package com.natamus.collective.config;

import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DuskConfigCustomFile {
	private static final List<DuskConfigCustomFile> customFiles = new CopyOnWriteArrayList<>();

	public final String modid;
	public final String fileName;
	public final String name;
	public final String description;
	public final String registry;
	public final String filter;
	public final boolean idValues;
	public final boolean multiline;
	public final Supplier<String> reader;
	public final BiConsumer<MinecraftServer, String> writer;
	public boolean chatColours = false;
	public boolean fixed = false;
	public List<String> variantIds = new ArrayList<>();
	public Supplier<String> defaultsReader = null;
	public String valueRegistry = "";
	public String valueFilter = "";
	public String valueType = "";

	private DuskConfigCustomFile(String modid, String fileName, String name, String description, String registry, String filter, boolean idValues, boolean multiline, Supplier<String> reader, BiConsumer<MinecraftServer, String> writer) {
		this.modid = modid;
		this.fileName = fileName;
		this.name = name;
		this.description = description;
		this.registry = registry;
		this.filter = filter;
		this.idValues = idValues;
		this.multiline = multiline;
		this.reader = reader;
		this.writer = writer;
	}

	public static void addIdList(String modid, String fileName, String name, String description, String registry, Supplier<String> reader, Consumer<String> writer) {
		addIdList(modid, fileName, name, description, registry, "", reader, writer);
	}
	public static void addIdList(String modid, String fileName, String name, String description, String registry, String filter, Supplier<String> reader, Consumer<String> writer) {
		addIdList(modid, fileName, name, description, registry, filter, reader, ignoringServer(writer));
	}
	public static void addIdList(String modid, String fileName, String name, String description, String registry, String filter, Supplier<String> reader, BiConsumer<MinecraftServer, String> writer) {
		customFiles.add(new DuskConfigCustomFile(modid, fileName, name, description, registry, filter, false, false, reader, writer));
	}

	public static void addIdValues(String modid, String fileName, String name, String description, String registry, Supplier<String> reader, Consumer<String> writer) {
		addIdValues(modid, fileName, name, description, registry, "", reader, writer);
	}
	public static void addIdValues(String modid, String fileName, String name, String description, String registry, String filter, Supplier<String> reader, Consumer<String> writer) {
		addIdValues(modid, fileName, name, description, registry, filter, reader, ignoringServer(writer));
	}
	public static void addIdValues(String modid, String fileName, String name, String description, String registry, String filter, Supplier<String> reader, BiConsumer<MinecraftServer, String> writer) {
		customFiles.add(new DuskConfigCustomFile(modid, fileName, name, description, registry, filter, true, false, reader, writer));
	}

	public static void addIdList(String modid, String fileName, String name, String description, String registry, List<String> variantIds, Supplier<String> reader, Consumer<String> writer) {
		addIdList(modid, fileName, name, description, registry, variantIds, reader, ignoringServer(writer));
	}
	public static void addIdList(String modid, String fileName, String name, String description, String registry, List<String> variantIds, Supplier<String> reader, BiConsumer<MinecraftServer, String> writer) {
		DuskConfigCustomFile customFile = new DuskConfigCustomFile(modid, fileName, name, description, registry, "", false, false, reader, writer);
		customFile.variantIds = new ArrayList<>(variantIds);
		customFiles.add(customFile);
	}

	public static void addIdValues(String modid, String fileName, String name, String description, String registry, List<String> variantIds, Supplier<String> reader, Consumer<String> writer) {
		addIdValues(modid, fileName, name, description, registry, variantIds, reader, ignoringServer(writer));
	}
	public static void addIdValues(String modid, String fileName, String name, String description, String registry, List<String> variantIds, Supplier<String> reader, BiConsumer<MinecraftServer, String> writer) {
		DuskConfigCustomFile customFile = new DuskConfigCustomFile(modid, fileName, name, description, registry, "", true, false, reader, writer);
		customFile.variantIds = new ArrayList<>(variantIds);
		customFiles.add(customFile);
	}

	public static void addFixedIdValues(String modid, String fileName, String name, String description, String registry, Supplier<String> reader, Consumer<String> writer) {
		addFixedIdValues(modid, fileName, name, description, registry, reader, ignoringServer(writer));
	}
	public static void addFixedIdValues(String modid, String fileName, String name, String description, String registry, Supplier<String> reader, BiConsumer<MinecraftServer, String> writer) {
		DuskConfigCustomFile customFile = new DuskConfigCustomFile(modid, fileName, name, description, registry, "", true, false, reader, writer);
		customFile.fixed = true;
		customFiles.add(customFile);
	}

	public static void addText(String modid, String fileName, String name, String description, Supplier<String> reader, Consumer<String> writer) {
		addText(modid, fileName, name, description, reader, ignoringServer(writer));
	}
	public static void addText(String modid, String fileName, String name, String description, Supplier<String> reader, BiConsumer<MinecraftServer, String> writer) {
		customFiles.add(new DuskConfigCustomFile(modid, fileName, name, description, "", "", false, true, reader, writer));
	}

	public static void addIdColours(String modid, String fileName, String name, String description, String registry, String filter, Supplier<String> reader, BiConsumer<MinecraftServer, String> writer) {
		DuskConfigCustomFile customFile = new DuskConfigCustomFile(modid, fileName, name, description, registry, filter, true, false, reader, writer);
		customFile.chatColours = true;
		customFiles.add(customFile);
	}

	public static void addIdMap(String modid, String fileName, String name, String description, String keyRegistry, String keyFilter, String valueRegistry, String valueFilter, String valueType, Supplier<String> reader, BiConsumer<MinecraftServer, String> writer) {
		DuskConfigCustomFile customFile = new DuskConfigCustomFile(modid, fileName, name, description, keyRegistry, keyFilter, false, false, reader, writer);
		customFile.valueRegistry = valueRegistry;
		customFile.valueFilter = valueFilter;
		customFile.valueType = valueType;
		customFiles.add(customFile);
	}

	public static void setDefaults(String modid, String fileName, Supplier<String> defaultsReader) {
		for (DuskConfigCustomFile customFile : customFiles) {
			if (customFile.modid.equals(modid) && customFile.fileName.equals(fileName)) {
				customFile.defaultsReader = defaultsReader;
			}
		}
	}

	private static BiConsumer<MinecraftServer, String> ignoringServer(Consumer<String> writer) {
		return (server, text) -> writer.accept(text);
	}

	public static List<DuskConfigCustomFile> getCustomFiles(String modid) {
		List<DuskConfigCustomFile> modFiles = new ArrayList<>();
		for (DuskConfigCustomFile customFile : getRegisteredFiles(modid)) {
			if (customFile.exists()) {
				modFiles.add(customFile);
			}
		}
		return modFiles;
	}

	public static DuskConfigCustomFile getCustomFile(String modid, String key) {
		for (DuskConfigCustomFile customFile : getRegisteredFiles(modid)) {
			if (customFile.getKey().equals(key)) {
				return customFile;
			}
		}
		return null;
	}

	private static List<DuskConfigCustomFile> getRegisteredFiles(String modid) {
		List<String> requestedCollectiveFiles = GenerateJSONFiles.getRequestedFiles(modid);

		List<DuskConfigCustomFile> registeredFiles = new ArrayList<>();
		for (DuskConfigCustomFile customFile : customFiles) {
			if (customFile.modid.equals(modid) || isSharedCollectiveFile(customFile, requestedCollectiveFiles)) {
				registeredFiles.add(customFile);
			}
		}
		return registeredFiles;
	}

	private static boolean isSharedCollectiveFile(DuskConfigCustomFile customFile, List<String> requestedCollectiveFiles) {
		return customFile.modid.equals(CollectiveReference.MOD_ID) && requestedCollectiveFiles.contains(customFile.fileName);
	}

	public boolean isIdMap() {
		return !valueRegistry.isEmpty();
	}

	public String getKey() {
		return fileName + "/" + name;
	}

	public Path getPath() {
		return DataFunctions.getConfigDirectoryPath().resolve(modid).resolve(fileName);
	}

	public boolean exists() {
		return Files.isRegularFile(getPath());
	}
}
