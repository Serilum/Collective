package com.natamus.collective.config;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.functions.NumberFunctions;
import com.natamus.collective.services.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** DuskConfig
 *  Based on <a href="https://github.com/TeamMidnightDust/MidnightLib">MidnightLib v2.2.0 Config by TeamMidnightDust and Motschen</a> */

@SuppressWarnings("unchecked")
public abstract class DuskConfig {
	private static final Pattern INTEGER_ONLY = Pattern.compile("(-?[0-9]*)");
	private static final Pattern DECIMAL_ONLY = Pattern.compile("-?(\\d+\\.?\\d*|\\d*\\.?\\d+|\\.)");
	private static final Pattern HEXADECIMAL_ONLY = Pattern.compile("(-?[#0-9a-fA-F]*)");

	private static final HashMap<String, List<EntryInfo>> entryHashMap = new HashMap<String, List<EntryInfo>>();

	public static class EntryInfo {
		Field field;
		Object widget;
		int width;
		double minValue;
		double maxValue;
		boolean minMaxSet = false;
		int max;
		boolean centered;
		Map.Entry<EditBox,Component> error;
		Object defaultValue;
		Object value;
		String tempValue;
		boolean inLimits = true;
		String id;
		Component name;
		String description = "";
		String range = "";
		int index;
		AbstractWidget colorButton;
	}

	public static final Map<String,Class<?>> configClass = new HashMap<>();
	private static final HashMap<String, String> modidToName = new HashMap<String, String>();
	private static final HashMap<String, Path> pathMap = new HashMap<String, Path>();
	private static final HashMap<String, HashMap<String, List<String>>> modConfigMetaData = new HashMap<String, HashMap<String, List<String>>>();

	private static final Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).excludeFieldsWithModifiers(Modifier.PRIVATE).addSerializationExclusionStrategy(new HiddenAnnotationExclusionStrategy()).setPrettyPrinting().create();

	public static void init(String name, String modid, Class<?> config) {
		init(name, modid, config, true);
	}
	public static void init(String name, String modid, Class<?> config, Boolean initial) {
		pathMap.put(modid, DataFunctions.getConfigDirectoryPath().resolve(modid + ".json5"));
		configClass.put(modid, config);
		modidToName.put(modid, name);
		entryHashMap.put(modid, new ArrayList<EntryInfo>());

		HashMap<String, List<String>> configMetaData = null;
		try {
			for (Field field : config.getDeclaredFields()) {
				if (field.getName().equals("configMetaData")) {
					configMetaData = (HashMap<String, List<String>>) field.get(config);
					break;
				}
			}
		}
		catch (Exception ignored) { }

		if (configMetaData != null) {
			modConfigMetaData.put(modid, configMetaData);
		}

		for (Field field : config.getFields()) {
			EntryInfo info = new EntryInfo();
			if ((field.isAnnotationPresent(Entry.class) || field.isAnnotationPresent(Comment.class)) && !field.isAnnotationPresent(Server.class) && !field.isAnnotationPresent(Hidden.class)) {
				if (Services.MODLOADER.isClientSide()) {
					initClient(modid, field, info, configMetaData);
				}
			}
			if (field.isAnnotationPresent(Comment.class)) {
				info.centered = field.getAnnotation(Comment.class).centered();
			}
			if (field.isAnnotationPresent(Entry.class)) {
				try {
					info.defaultValue = field.get(null);
				} catch (IllegalAccessException ignored) {
				}
			}
		}

		try {
			loadJson(pathMap.get(modid), modid);
		}
		catch (Exception e) {
			write(modid);
		}

		try {
			for (EntryInfo info : entryHashMap.get(modid)) {
				if (info.field.isAnnotationPresent(Entry.class)) {
					try {
						info.value = info.field.get(null);
						info.tempValue = info.value.toString();
					}
					catch (IllegalAccessException ignored) { }
				}
			}
		}
		catch (NullPointerException ex) {
			if (initial) {
				init(name, modid, config, false);
			}
		}
	}

	private static void initClient(String modid, Field field, EntryInfo info, HashMap<String, List<String>> configMetaData) {
		Class<?> type = field.getType();
		Entry e = field.getAnnotation(Entry.class);
		info.width = e != null ? e.width() : 0;
		info.field = field;
		info.id = modid;

		if (e != null) {
			if (!e.name().equals("")) {
				info.name = Component.translatable(e.name());
			}
			if (type == int.class) {
				if (e.min() != Double.MIN_NORMAL) { info.minValue = (int) e.min(); info.maxValue = (int) e.max(); info.minMaxSet = true; }
				textField(modid, info, Integer::parseInt, INTEGER_ONLY, (int) e.min(), (int) e.max(), true);
			}
			else if (type == float.class) {
				if (e.min() != Double.MIN_NORMAL) { info.minValue = (int) e.min(); info.maxValue = (int) e.max(); info.minMaxSet = true; }
				textField(modid, info, Float::parseFloat, DECIMAL_ONLY, (float) e.min(), (float) e.max(), false);
			}
			else if (type == double.class) {
				if (e.min() != Double.MIN_NORMAL) { info.minValue = (int) e.min(); info.maxValue = (int) e.max(); info.minMaxSet = true; }
				textField(modid, info, Double::parseDouble, DECIMAL_ONLY, e.min(), e.max(), false);
			}
			else if (type == String.class || type == List.class) {
				info.max = e.max() == Double.MAX_VALUE ? Integer.MAX_VALUE : (int) e.max();
				textField(modid, info, String::length, null, Math.min(e.min(), 0), Math.max(e.max(), 1), true);
			} else if (type == boolean.class) {
				Function<Object, Component> func = value -> Component.translatable((Boolean) value ? "gui.yes" : "gui.no").withStyle((Boolean) value ? ChatFormatting.GREEN : ChatFormatting.RED);
				info.widget = new AbstractMap.SimpleEntry<Button.OnPress, Function<Object, Component>>(button -> {
					info.value = !(Boolean) info.value;
					button.setMessage(func.apply(info.value));
				}, func);
			} else if (type.isEnum()) {
				List<?> values = Arrays.asList(field.getType().getEnumConstants());
				Function<Object, Component> func = value -> Component.translatable(modid + ".duskconfig." + "enum." + type.getSimpleName() + "." + info.value.toString());
				info.widget = new AbstractMap.SimpleEntry<Button.OnPress, Function<Object, Component>>(button -> {
					int index = values.indexOf(info.value) + 1;
					info.value = values.get(index >= values.size() ? 0 : index);
					button.setMessage(func.apply(info.value));
				}, func);
			}
		}

		String fieldname = field.getName();
		if (configMetaData != null) {
			if (configMetaData.containsKey(fieldname)) {
				List<String> metaData = configMetaData.get(fieldname);
				if (metaData.size() > 0) {
					info.description = metaData.get(0);
					if (metaData.size() > 1) {
						info.range = metaData.get(1);
					}
				}
			}
		}

		if (!entryHashMap.containsKey(modid)) {
			entryHashMap.put(modid, new ArrayList<EntryInfo>());
		}

		try {
			entryHashMap.get(modid).add(info);
		}
		catch (NullPointerException ex) {
			entryHashMap.put(modid, Arrays.asList(info));
		}
	}

	private static void textField(String modid, EntryInfo info, Function<String,Number> f, Pattern pattern, double min, double max, boolean cast) {
		boolean isNumber = pattern != null;
		info.widget = (BiFunction<EditBox, Button, Predicate<String>>) (t, b) -> s -> {
			if (!(s.isEmpty() || !isNumber || pattern.matcher(s).matches())) return false;

			Number value = 0;
			boolean inLimits = false;
			info.error = null;
			if (!(isNumber && s.isEmpty()) && !s.equals("-") && !s.equals(".")) {
				value = f.apply(s);
				inLimits = value.doubleValue() >= min && value.doubleValue() <= max;
				info.error = inLimits? null : new AbstractMap.SimpleEntry<>(t, Component.literal(value.doubleValue() < min ?
						"§cMinimum " + (isNumber? "value" : "length") + (cast? " is " + (int)min : " is " + min) :
						"§cMaximum " + (isNumber? "value" : "length") + (cast? " is " + (int)max : " is " + max)));
			}

			info.tempValue = s;
			t.setTextColor(inLimits? 0xFFFFFFFF : 0xFFFF7777);
			info.inLimits = inLimits;
			b.active = entryHashMap.get(modid).stream().allMatch(e -> e.inLimits);

			if (inLimits && info.field.getType() != List.class)
				info.value = isNumber? value : s;
			else if (inLimits) {
				if (((List<String>) info.value).size() == info.index) ((List<String>) info.value).add("");
				((List<String>) info.value).set(info.index, Arrays.stream(info.tempValue.replace("[", "").replace("]", "").split(", ")).toList().getFirst());
			}

			if (info.field.getAnnotation(Entry.class).isColor()) {
				if (!s.contains("#")) s = '#' + s;
				if (!HEXADECIMAL_ONLY.matcher(s).matches()) return false;
				try {
					info.colorButton.setMessage(Component.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));
				} catch (Exception ignored) {}
			}
			return true;
		};
	}

	public static void write(String modid) {
		Path path = DataFunctions.getConfigDirectoryPath().resolve(modid + ".json5");
		pathMap.put(modid, path);

		try {
			if (!Files.exists(path)) {
				Files.createDirectories(path.getParent());
				Files.createFile(path);
			}

			Class<?> cC = configClass.get(modid);
			if (cC == null) {
				return;
			}

			HashMap<String, List<String>> configMetaData = null;

			String lastline = "";
			StringBuilder json5 = new StringBuilder("{");
			for (Field field : cC.getDeclaredFields()) {
				if (field.getName().equals("configMetaData")) {
					configMetaData = (HashMap<String, List<String>>) field.get(cC);
					break;
				}
			}

			for (Field field : cC.getDeclaredFields()) {
				String fieldname = field.getName();
				if (fieldname.equals("configMetaData")) {
					continue;
				}

				if (configMetaData != null) {
					if (configMetaData.containsKey(fieldname)) {
						List<String> metaData = configMetaData.get(fieldname);
						for (String metaDataValue : metaData) {
							json5.append("\n\t// ").append(metaDataValue);
						}

						if (field.isAnnotationPresent(Entry.class)) {
							Entry e = field.getAnnotation(Entry.class);
							if (e.min() != Double.MIN_NORMAL) {
								String rangeValue = "min: " + e.min() + ", max: " + e.max();
								if (field.getType() == int.class) {
									rangeValue = rangeValue.replace(".0", "");
								}
								json5.append("\n\t// ").append(rangeValue);
							}
						}
					}
				}

				if (field.getType().isAssignableFrom(String.class)){
					String fieldvalue = field.get(cC).toString();
					if (fieldvalue.length() > 0) {
						if (fieldvalue.charAt(0) != '"') {
							fieldvalue = '"' + fieldvalue + '"';
						}
					}
					else {
						fieldvalue = "\"\"";
					}

					json5.append("\n\t\"").append(fieldname).append("\": ").append(fieldvalue).append(",");
				}
				else {
					json5.append("\n\t\"").append(fieldname).append("\": ").append(field.get(cC)).append(",");
				}
			}

			json5.setLength(json5.length() - 1);
			json5.append("\n}");

			Files.write(path, json5.toString().getBytes());
		} catch (Exception ignored) { }
	}

	public static class DuskConfigScreen extends Screen {
		protected DuskConfigScreen(Screen parent, String modid) {
			super(Component.literal(StringUtils.capitalize(modidToName.get(modid)) + " Config"));
			this.parent = parent;
			this.modid = modid;
			this.translationPrefix = modid + ".duskconfig.";
		}
		public final String translationPrefix;
		public final Screen parent;
		public final String modid;
		public DuskConfigListWidget list;
		public boolean reload = false;

		public static Screen getScreen(Screen parent, String modid) {
			return new DuskConfigScreen(parent, modid);
		}

		@Override
		public void renderBackground(@NotNull GuiGraphics guiGraphics, int a, int b, float c) {
			if (this.minecraft.level == null) {
				this.renderPanorama(guiGraphics, c);
			}

			this.renderBlurredBackground(c);
		}

		// Real Time config update //
		@Override
		public void tick() {
			super.tick();
			for (EntryInfo info : entryHashMap.get(modid)) {
				try {info.field.set(null, info.value);} catch (IllegalAccessException ignored) {}
			}
			updateResetButtons();
		}
		public void updateResetButtons() {
			if (this.list != null) {
				for (ButtonEntry entry : this.list.children()) {
					if (entry.buttons != null && entry.buttons.size() > 1 && entry.buttons.get(1) instanceof Button button) {
						button.active = !Objects.equals(entry.info.value.toString(), entry.info.defaultValue.toString());
					}
				}
			}
		}
		public void loadValues() {
			try {
				loadJson(pathMap.get(modid), modid);
			}
			catch (Exception e) {
				write(modid);
			}

			for (EntryInfo info : entryHashMap.get(modid)) {
				if (info.field.isAnnotationPresent(Entry.class))
					try {
						info.value = info.field.get(null);
						info.tempValue = info.value.toString();
					} catch (IllegalAccessException ignored) {}
			}
		}
		@Override
		public void init() {
			super.init();
			if (!reload) loadValues();

			this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
				loadValues();
				Objects.requireNonNull(minecraft).setScreen(parent);
			}).pos(this.width / 2 - 154, this.height - 28).size(150, 20).build());

			Button done = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
				for (EntryInfo info : entryHashMap.get(modid))
					if (info.id.equals(modid)) {
						try {
							info.field.set(null, info.value);
						} catch (IllegalAccessException ignored) {}
					}
				write(modid);
				Objects.requireNonNull(minecraft).setScreen(parent);
			}).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build());

			this.list = new DuskConfigListWidget(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
			// if (this.minecraft != null && this.minecraft.level != null) this.list.setRenderBackground(false);

			this.addWidget(this.list);
			for (EntryInfo info : entryHashMap.get(modid)) {
				if (info.id.equals(modid)) {
					String namestring = info.field.getName();
					namestring = namestring.replace("_", " ").strip();

					String[] nsspl = namestring.split("(?<=.)(?=\\p{Lu})");
					List<String> correctNameParts = new ArrayList<String>();

					try {
						for (int i = 0; i < nsspl.length; i++) {
							StringBuilder namePart = new StringBuilder(nsspl[i]);
							if (namePart.toString().strip().length() == 1) {
								boolean append = false;
								while (i < nsspl.length) {
									if (append) {
										namePart.append(nsspl[i]);
									}

									if (i + 1 >= nsspl.length) {
										break;
									}

									if (nsspl[i + 1].strip().length() == 1) {
										append = true;
										i += 1;
										continue;
									}
									break;
								}
							}

							if (!correctNameParts.contains(namePart.toString())) {
								correctNameParts.add(namePart.toString());
							}
						}
					}
					catch (Exception ex) {
						correctNameParts = new ArrayList<String>(Arrays.asList(nsspl));
					}

					String formattedName = StringUtils.capitalize(String.join(" ", correctNameParts));

					Component name = Component.literal(formattedName);
					Button resetButton = Button.builder(Component.literal("Reset").withStyle(ChatFormatting.RED), button -> {
						info.value = info.defaultValue;
						info.tempValue = info.defaultValue.toString();
						info.index = 0;
						double scrollAmount = list.getScrollAmount();
						this.reload = true;
						Objects.requireNonNull(minecraft).setScreen(this);
						list.setScrollAmount(scrollAmount);
					}).pos(width - 205, 0).size(40, 20).build();

					if (info.widget instanceof Map.Entry) {
						Map.Entry<Button.OnPress, Function<Object, Component>> widget = (Map.Entry<Button.OnPress, Function<Object, Component>>) info.widget;
						if (info.field.getType().isEnum()) widget.setValue(value -> Component.translatable(translationPrefix + "enum." + info.field.getType().getSimpleName() + "." + info.value.toString()));
						this.list.addButton(List.of(Button.builder(widget.getValue().apply(info.value), widget.getKey()).pos(width - 160, 0).size(150, 20).build(), resetButton), name, info);
					} else if (info.field.getType() == List.class) {
						if (!reload) info.index = 0;
						EditBox widget = new EditBox(font, width - 160, 0, 150, 20, null);
						widget.setMaxLength(Integer.MAX_VALUE);
						if (info.index < ((List<String>)info.value).size()) widget.setValue((String.valueOf(((List<String>)info.value).get(info.index))));
						else widget.setValue("");
						Predicate<String> processor = ((BiFunction<EditBox, Button, Predicate<String>>) info.widget).apply(widget, done);
						widget.setFilter(processor);
						resetButton.setWidth(20);
						resetButton.setMessage(Component.literal("R").withStyle(ChatFormatting.RED));
						Button cycleButton = Button.builder(Component.literal(String.valueOf(info.index)), button -> {
							((List<String>)info.value).remove("");
							double scrollAmount = list.getScrollAmount();
							this.reload = true;
							info.index = info.index + 1;
							if (info.index > ((List<String>)info.value).size()) info.index = 0;
							Objects.requireNonNull(minecraft).setScreen(this);
							list.setScrollAmount(scrollAmount);
						}).pos(width - 185, 0).size(20, 20).build();
						this.list.addButton(List.of(widget, resetButton, cycleButton), name, info);
					} else if (info.widget != null) {
						EditBox widget = new EditBox(font, width - 160, 0, 150, 20, null);
						widget.setMaxLength(Integer.MAX_VALUE);
						widget.setValue(info.tempValue);
						Predicate<String> processor = ((BiFunction<EditBox, Button, Predicate<String>>) info.widget).apply(widget, done);
						widget.setFilter(processor);
						if (info.field.getAnnotation(Entry.class).isColor()) {
							resetButton.setWidth(20);
							resetButton.setMessage(Component.literal("R").withStyle(ChatFormatting.RED));
							Button colorButton = Button.builder(Component.literal("⬛"), (button -> {})).pos(width - 185, 0).size(20, 20).build();
							try {colorButton.setMessage(Component.literal("⬛").setStyle(Style.EMPTY.withColor(Color.decode(info.tempValue).getRGB())));} catch (Exception ignored) {}
							info.colorButton = colorButton;
							colorButton.active = false;
							this.list.addButton(List.of(widget, resetButton, colorButton), name, info);
						}
						else this.list.addButton(List.of(widget, resetButton), name, info);
					} else {
						this.list.addButton(List.of(), name, info);
					}

					String rangeValue = "";
					if (info.minMaxSet) {
						rangeValue = "min: " + info.minValue + ", max: " + info.maxValue;
						if (info.field.getType() == int.class) {
							rangeValue = rangeValue.replace(".0", "");
						}
					}
					else if (!info.range.equals("")) {
						rangeValue = info.range;
					}

					if (!rangeValue.equals("")) {
						EditBox label = new EditBox(font, width - 155, 0, 145, 20, null);
						label.setValue(rangeValue);
						label.setBordered(false);
						label.setEditable(false);
						this.list.addButton(List.of(label), Component.literal(""), info);
					}
				}
				updateResetButtons();
			}

		}
		@Override
		public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
			super.render(guiGraphics, mouseX, mouseY, delta);

			if (this.minecraft == null || Objects.requireNonNull(minecraft).level == null) {
				guiGraphics.pose().translate(0, 0, -100);
			}
			this.list.render(guiGraphics, mouseX, mouseY, delta);
			guiGraphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
		}
	}

	public static class DuskConfigListWidget extends ContainerObjectSelectionList<ButtonEntry> {
		Font font;

		public DuskConfigListWidget(Minecraft client, int width, int height, int y, int itemHeight, int m) {
			super(client, width, height-68, y, m);
			this.centerListVertically = false;
			font = client.font;
		}
		@Override
		public int getScrollbarPosition() { return this.width -7; }

		public void addButton(List<AbstractWidget> buttons, Component text, EntryInfo info) {
			this.addEntry(ButtonEntry.create(buttons, text, info));
		}
		@Override
		public int getRowWidth() { return 10000; }
		public Optional<AbstractWidget> getHoveredButton(double mouseX, double mouseY) {
			for (ButtonEntry buttonEntry : this.children()) {
				if (!buttonEntry.buttons.isEmpty() && buttonEntry.buttons.getFirst().isMouseOver(mouseX, mouseY)) {
					return Optional.of(buttonEntry.buttons.getFirst());
				}
			}
			return Optional.empty();
		}
	}
	public static class ButtonEntry extends ContainerObjectSelectionList.Entry<ButtonEntry> {
		private static final Font font = Minecraft.getInstance().font;
		public final List<AbstractWidget> buttons;
		private final Component text;
		public final EntryInfo info;
		private final List<AbstractWidget> children = new ArrayList<AbstractWidget>();
		public static final Map<AbstractWidget, Component> buttonsWithComponent = new HashMap<>();

		private ButtonEntry(List<AbstractWidget> buttons, Component text, EntryInfo info) {
			if (!buttons.isEmpty()) buttonsWithComponent.put(buttons.getFirst(),text);
			this.buttons = buttons;
			this.text = text;
			this.info = info;
			children.addAll(buttons);
		}
		public static ButtonEntry create(List<AbstractWidget> buttons, Component text, EntryInfo info) {
			return new ButtonEntry(buttons, text, info);
		}
		public void render(@NotNull GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			buttons.forEach(b -> { b.setY(y); b.render(matrices, mouseX, mouseY, tickDelta); });
			if (text != null && (!text.getString().contains("spacer") || !buttons.isEmpty())) {
				if (info.centered) {
					matrices.drawString(font, text, (int)(Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2f - (font.width(text) / 2f)), y + 5, 0xFFFFFF); // TODO CHECK
				}
				else {
					matrices.drawString(font, text, 12, y + 5, 0xFFFFFF);
				}
			}
		}
		public @NotNull List<? extends GuiEventListener> children() {return children;}
		public @NotNull List<? extends NarratableEntry> narratables() {return children;}
	}
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Entry {
		int width() default 100;
		double min() default Double.MIN_NORMAL;
		double max() default Double.MAX_VALUE;
		String name() default "";
		boolean isColor() default false;
	}
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Client {}
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Server {}
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Hidden {}
	@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) public @interface Comment {
		boolean centered() default false;
	}

	public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
		public boolean shouldSkipClass(Class<?> clazz) { return false; }
		public boolean shouldSkipField(FieldAttributes fieldAttributes) {
			return fieldAttributes.getAnnotation(Entry.class) == null;
		}
	}

	public static void loadJson(Path path, String modid) throws JsonSyntaxException, IOException {
		boolean rewriteConfig = false;

		// backwards compatibility
		Path oldFabricJsonPath = DataFunctions.getConfigDirectoryPath().resolve(modid + ".json");
		if (Files.exists(oldFabricJsonPath)) {
			Files.move(oldFabricJsonPath, path, StandardCopyOption.REPLACE_EXISTING);
			rewriteConfig = true;
		}

		Path oldForgeTomlPath = DataFunctions.getConfigDirectoryPath().resolve(modid + "-common.toml");
		if (Files.exists(oldForgeTomlPath)) {
			convertTomlToJson(oldForgeTomlPath, path);
			rewriteConfig = true;
		}

		if (!Files.exists(path)) {
			rewriteConfig = true;
		}

		String content = Files.readString(path);
		content = Stream.of(content.split("\n")).filter(s -> !s.startsWith("\t//")).collect(Collectors.joining("\n"));

		gson.fromJson(new StringReader(content), configClass.get(modid));

		if (rewriteConfig) {
			write(modid);
		}
	}

	private static void convertTomlToJson(Path oldTomlPath, Path newJsonPath) throws IOException {
		JsonWriter jsonWriter = new JsonWriter(new FileWriter(newJsonPath.toString()));
		jsonWriter.beginObject();

		String tomlContent = Files.readString(oldTomlPath, StandardCharsets.UTF_8);
		for (String line : tomlContent.split("\n")) {
			line = line.trim();
			if (line.startsWith("[") || line.startsWith("#")) {
				continue;
			}

			String[] linespl = line.split(" = ", 2);
			if (linespl.length != 2) {
				continue;
			}

			String key = linespl[0];
			String rawValue = linespl[1].strip();

			if (rawValue.equals("true") || rawValue.equals("false")) {
				jsonWriter.name(key).value(Boolean.parseBoolean(rawValue));
			} else if (NumberFunctions.isNumeric(rawValue)) {
				if (rawValue.contains(".")) {
					jsonWriter.name(key).value(Double.parseDouble(rawValue));
				} else {
					jsonWriter.name(key).value(Integer.parseInt(rawValue));
				}
			} else {
				jsonWriter.name(key).value(rawValue);
			}
		}

		jsonWriter.endObject();
		jsonWriter.close();
		Files.delete(oldTomlPath);
	}
}