package com.natamus.collective.config;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DuskConfigCustomFileEntry extends DuskConfigEntry {
	public final DuskConfigCustomFile customFile;

	public DuskConfigCustomFileEntry(DuskConfigCustomFile customFile, String text) {
		super(customFile.name, List.of(customFile.description), customFile.registry, customFile.filter, customFile.idValues, customFile.multiline, text);
		this.customFile = customFile;
	}

	public DuskConfigCustomFileEntry(DuskConfigCustomFile customFile) {
		this(customFile, customFile.reader.get());
	}

	public void save(MinecraftServer server) {
		customFile.writer.accept(server, getText());
	}

	@Override
	public boolean hasChatColourValues() {
		return customFile.chatColours;
	}

	@Override
	public boolean isIdMap() {
		return customFile.isIdMap();
	}

	@Override
	public boolean isFixedList() {
		return customFile.fixed;
	}

	@Override
	public List<String> getVariantIds() {
		return customFile.variantIds;
	}

	public DuskConfigValueEntry getMapValueEntry(String displayName, String values, Consumer<String> onChange) {
		return new DuskConfigValueEntry(displayName, customFile.valueRegistry, customFile.valueFilter, customFile.valueType, values, onChange);
	}

	@Override
	public List<String> getIdList() {
		if (!isIdMap()) {
			return super.getIdList();
		}
		return getMapLines(getText());
	}

	@Override
	public List<String> getDefaultIdList() {
		if (customFile.defaultsReader == null) {
			return new ArrayList<>();
		}

		String defaultText = customFile.defaultsReader.get();
		if (isIdMap()) {
			return getMapLines(defaultText);
		}
		return splitList(defaultText);
	}

	private static List<String> getMapLines(String text) {
		List<String> mapLines = new ArrayList<>();
		for (String line : text.split("\\R")) {
			if (!line.isBlank()) {
				mapLines.add(line.strip());
			}
		}
		return mapLines;
	}

	@Override
	public void setIdList(List<String> idList) {
		if (!isIdMap()) {
			super.setIdList(idList);
			return;
		}
		setValue(String.join("\n", idList));
	}
}
