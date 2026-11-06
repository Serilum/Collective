package com.natamus.collective.config;

import java.util.function.Consumer;

public class DuskConfigValueEntry extends DuskConfigEntry {
	private final Consumer<String> onChange;

	public DuskConfigValueEntry(String displayName, String registry, boolean weights, String value, Consumer<String> onChange) {
		super(displayName, registry, weights, value);
		this.onChange = onChange;
	}

	@Override
	public void setValue(Object value) {
		super.setValue(value);
		onChange.accept(String.valueOf(value));
	}
}
