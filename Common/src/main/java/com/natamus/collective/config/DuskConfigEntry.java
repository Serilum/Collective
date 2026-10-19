package com.natamus.collective.config;

import com.natamus.collective.functions.ColourFunctions;
import com.natamus.collective.functions.StringFunctions;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DuskConfigEntry {
	public final Field field;
	public final List<String> description;
	public final List<String> options = new ArrayList<>();
	public final String registry;
	public final String filter;
	public final boolean single;
	public final boolean amounts;
	private final String name;
	private final String displayName;
	private final Class<?> type;
	private final Type genericType;
	private final boolean rgb;
	private final Object defaultValue;
	private final double min;
	private final double max;
	private Object value;

	public DuskConfigEntry(Field field, List<String> description) {
		DuskConfig.Entry entry = field.getAnnotation(DuskConfig.Entry.class);

		this.field = field;
		this.description = description;
		this.name = field.getName();
		this.displayName = getFieldDisplayName(field.getName());
		this.type = field.getType();
		this.genericType = field.getGenericType();
		this.defaultValue = copyValue(getValue());
		this.min = entry.min();
		this.max = entry.max();
		this.registry = entry.registry();
		this.filter = entry.filter();
		this.single = entry.single();
		this.amounts = entry.amounts();
		this.rgb = entry.rgb();

		if (type.isEnum()) {
			for (Object enumConstant : type.getEnumConstants()) {
				options.add(((Enum<?>) enumConstant).name());
			}
		}
		else {
			options.addAll(List.of(entry.options()));
		}
	}

	public DuskConfigEntry(String name, List<String> description, boolean defaultValue) {
		this.field = null;
		this.description = description;
		this.name = name;
		this.displayName = name;
		this.type = boolean.class;
		this.genericType = boolean.class;
		this.value = defaultValue;
		this.defaultValue = defaultValue;
		this.min = Double.MIN_NORMAL;
		this.max = Double.MAX_VALUE;
		this.registry = "";
		this.filter = "";
		this.single = false;
		this.amounts = false;
		this.rgb = false;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Type getGenericType() {
		return genericType;
	}

	public String getComment() {
		List<String> comment = new ArrayList<>(description);
		if (hasRange()) {
			comment.add("min: " + getMinText() + ", max: " + getMaxText());
		}
		if (hasOptions()) {
			comment.add("options: " + String.join(", ", options));
		}
		return String.join(" ", comment);
	}

	public Object getValue() {
		if (field == null) {
			return value;
		}

		try {
			return field.get(null);
		}
		catch (IllegalAccessException ignored) { }
		return defaultValue;
	}

	public void setValue(Object value) {
		if (field == null) {
			this.value = value;
			return;
		}

		try {
			field.set(null, value);
		}
		catch (IllegalAccessException ignored) { }
	}

	public void resetValue() {
		setValue(copyValue(defaultValue));
	}

	public boolean isDefault() {
		return Objects.equals(getValue(), defaultValue);
	}

	public boolean isBoolean() {
		return type == boolean.class;
	}

	public boolean isNumber() {
		return type == int.class || type == float.class || type == double.class;
	}

	public boolean isText() {
		return type == String.class && !hasOptions() && !hasRegistry() && !isRgb();
	}

	public boolean isChatColour() {
		return type == int.class && getName().toLowerCase(Locale.ROOT).contains("colour") && min == 0 && max == 15;
	}

	public boolean isRgb() {
		return rgb && type == String.class;
	}

	public boolean hasRegistry() {
		return !registry.isEmpty() && type == String.class;
	}

	public boolean hasRange() {
		return isNumber() && min != Double.MIN_NORMAL;
	}

	public boolean hasOptions() {
		return !options.isEmpty();
	}

	public String getMinText() {
		return formatNumber(min);
	}

	public String getMaxText() {
		return formatNumber(max);
	}

	public int getRgbColour() {
		return ColourFunctions.getRgbColour(getText());
	}

	public String getOption() {
		if (getValue() instanceof Enum<?> enumValue) {
			return enumValue.name();
		}
		return String.valueOf(getValue());
	}

	public String getNextOption() {
		int index = options.indexOf(getOption()) + 1;
		if (index >= options.size()) {
			index = 0;
		}
		return options.get(index);
	}

	public String getPreviousOption() {
		int index = options.indexOf(getOption()) - 1;
		if (index < 0) {
			index = options.size() - 1;
		}
		return options.get(index);
	}

	public String getOptionLabel(String option) {
		if (type.isEnum()) {
			return StringFunctions.capitalizeEveryWord(option.replace("_", " "));
		}
		return option;
	}

	public String findOption(String rawOption) {
		for (String option : options) {
			if (option.equalsIgnoreCase(rawOption.strip())) {
				return option;
			}
		}
		return null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void setOption(String option) {
		if (type.isEnum()) {
			setValue(Enum.valueOf((Class<Enum>) type, option));
		}
		else {
			setValue(option);
		}
	}

	public List<String> getIdList() {
		return splitList(String.valueOf(getValue()));
	}

	public void setIdList(List<String> idList) {
		setValue(String.join(",", idList));
	}

	public String getText() {
		return getValueText(getValue());
	}

	public String getDefaultText() {
		return getValueText(defaultValue);
	}

	public boolean setValueFromText(String text) {
		if (isBoolean()) {
			if (!text.equalsIgnoreCase("true") && !text.equalsIgnoreCase("false")) {
				return false;
			}

			setValue(Boolean.parseBoolean(text));
			return true;
		}

		if (hasOptions()) {
			String option = findOption(text);
			if (option == null) {
				return false;
			}

			setOption(option);
			return true;
		}

		if (isRgb()) {
			int colour = ColourFunctions.getRgbColour(text);
			if (colour == -1) {
				return false;
			}

			setValue(ColourFunctions.getRgbString(colour));
			return true;
		}

		Object value = getValueFromText(text);
		if (value == null) {
			return false;
		}

		setValue(value);
		return true;
	}

	public Object getValueFromText(String text) {
		try {
			if (type == int.class) {
				return getIfValid(Integer.parseInt(text.strip()));
			}
			if (type == float.class) {
				return getIfValid(Float.parseFloat(text.strip()));
			}
			if (type == double.class) {
				return getIfValid(Double.parseDouble(text.strip()));
			}
		}
		catch (NumberFormatException ignored) {
			return null;
		}

		if (type == List.class) {
			return splitList(text);
		}
		return text;
	}

	public boolean isValidNumber(Number value) {
		double number = value.doubleValue();
		if (!Double.isFinite(number)) {
			return false;
		}
		if (!hasRange()) {
			return true;
		}

		if (type == float.class) {
			float floatNumber = value.floatValue();
			return floatNumber >= (float) min && floatNumber <= (float) max;
		}
		return number >= min && number <= max;
	}

	private Number getIfValid(Number value) {
		if (!isValidNumber(value)) {
			return null;
		}
		return value;
	}

	private String formatNumber(double number) {
		if (type == int.class || number == Math.rint(number)) {
			return NumberFormat.getIntegerInstance(Locale.US).format((long) number);
		}
		return BigDecimal.valueOf(number).stripTrailingZeros().toPlainString();
	}

	private static List<String> splitList(String text) {
		List<String> list = new ArrayList<>();
		for (String part : text.split(",")) {
			if (!part.isBlank()) {
				list.add(part.strip());
			}
		}
		return list;
	}

	private static String getValueText(Object value) {
		if (value instanceof List<?> list) {
			List<String> parts = new ArrayList<>();
			for (Object part : list) {
				parts.add(String.valueOf(part));
			}
			return String.join(", ", parts);
		}
		return String.valueOf(value);
	}

	private static String getFieldDisplayName(String fieldName) {
		return StringFunctions.capitalizeFirst(StringFunctions.splitCamelCase(fieldName.replace("_", " ")));
	}

	private static Object copyValue(Object value) {
		if (value instanceof List<?> list) {
			return new ArrayList<>(list);
		}
		return value;
	}
}
