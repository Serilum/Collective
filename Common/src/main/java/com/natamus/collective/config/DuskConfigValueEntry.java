package com.natamus.collective.config;

import java.util.function.Consumer;

public class DuskConfigValueEntry extends DuskConfigEntry {
	private final Consumer<String> onChange;
	private final boolean levelDurations;

	public DuskConfigValueEntry(String displayName, String registry, boolean weights, String value, Consumer<String> onChange) {
		super(displayName, registry, weights, value);
		this.onChange = onChange;
		this.levelDurations = false;
	}

	public DuskConfigValueEntry(String displayName, String registry, String filter, String valueType, String value, Consumer<String> onChange) {
		super(displayName, registry, filter, valueType.equals("single"), valueType.equals("weights"), hasNumberPerId(valueType), value);
		this.onChange = onChange;
		this.levelDurations = valueType.equals("levelduration");
	}

	private static boolean hasNumberPerId(String valueType) {
		return valueType.equals("values") || valueType.equals("levelduration");
	}

	@Override
	public boolean hasLevelDurationValues() {
		return levelDurations;
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);
		onChange.accept(String.valueOf(value));
	}
}
