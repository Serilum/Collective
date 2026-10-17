package com.natamus.collective.config.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.config.DuskConfigEntry;
import com.natamus.collective.config.DuskConfigFile;
import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.config.DuskConfigModToggles;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DuskConfigEditScreen extends DuskConfigBaseScreen {
	public static boolean showTooltips = false;

	private final Screen parent;
	private final DuskConfigMod configMod;
	private final List<DuskConfigSetting> settings = new ArrayList<>();
	private final List<String> initialValues = new ArrayList<>();
	private DuskConfigList list;
	private EditBox searchBox;
	private Button doneButton;
	private String search = "";

	public DuskConfigEditScreen(Screen parent, DuskConfigMod configMod) {
		super(Component.translatable("collective.collective.gui.configtitle", StringUtils.capitalize(configMod.name)));
		this.parent = parent;
		this.configMod = configMod;

		for (DuskConfigEntry entry : configMod.entries) {
			initialValues.add(entry.getText());
		}
	}

	@Override
	protected boolean hasUnsavedChanges() {
		for (int i = 0; i < configMod.entries.size(); i++) {
			if (!configMod.entries.get(i).getText().equals(initialValues.get(i))) {
				return true;
			}
		}
		return false;
	}

	public static Screen getScreen(Screen parent, String modid) {
		if (!DuskConfigModToggles.hasConfigScreen(modid)) {
			return parent;
		}

		DuskConfigMod configMod = DuskConfigMod.addListOnlyMod(modid);
		DuskConfigFile.reload(configMod);
		return new DuskConfigEditScreen(parent, configMod);
	}

	@Override
	protected void init() {
		super.init();

		settings.clear();
		for (DuskConfigEntry entry : configMod.getActiveEntries()) {
			settings.add(new DuskConfigSetting(this, entry, this.width - 3));
		}

		searchBox = this.addRenderableWidget(new EditBox(this.font, this.width / 2 - 70, 28, 140, 20, Component.translatable("gui.recipebook.search_hint")));
		searchBox.setHint(Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.DARK_GRAY));
		searchBox.setValue(search);
		searchBox.setResponder(text -> {
			search = text;
			refreshRows();
			list.setScrollAmount(0);
		});

		ChatFormatting tooltipColour = ChatFormatting.DARK_GRAY;
		Component tooltipState = CommonComponents.OPTION_OFF;
		if (showTooltips) {
			tooltipColour = ChatFormatting.WHITE;
			tooltipState = CommonComponents.OPTION_ON;
		}

		Button tooltipButton = this.addRenderableWidget(Button.builder(Component.literal("?").withStyle(tooltipColour), button -> {
			showTooltips = !showTooltips;
			this.rebuildWidgets();
		}).pos(this.width - 67, 28).size(20, 20).build());
		tooltipButton.setTooltip(Tooltip.create(Component.translatable("collective.collective.gui.tooltips", tooltipState)));

		Button guiScaleButton = this.addRenderableWidget(new DuskConfigButton(this.width - 43, 28, 30, 20, getGuiScaleLabel(), button -> cycleGuiScale(false), button -> cycleGuiScale(true)));
		guiScaleButton.setTooltip(Tooltip.create(Component.translatable("options.generic_value", Component.translatable("options.guiScale"), getGuiScaleLabel())));

		Component patreonLabel = Component.literal("❤ ").withStyle(ChatFormatting.RED).append(Component.literal("Patreon").withStyle(ChatFormatting.WHITE));
		this.addRenderableWidget(new DuskConfigButton(10, 28, this.font.width(patreonLabel) + 12, 20, patreonLabel, ConfirmLinkScreen.confirmLink(this, URI.create("https://link.serilum.com/patreon"), true)));

		double scroll = 0;
		if (list != null) {
			scroll = list.scrollAmount();
		}

		list = new DuskConfigList(this.minecraft, this.width, this.height - 92, 56, 25);
		this.addWidget(list);
		refreshRows();
		list.setScrollAmount(scroll);

		addCancelButton(this.width / 2 - 154, this.height - 28);
		doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> saveAndClose()).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build());

		onSettingChanged();
	}

	private void refreshRows() {
		String searchText = search.strip().toLowerCase(Locale.ROOT);

		List<DuskConfigRow> rows = new ArrayList<>();
		for (DuskConfigSetting setting : settings) {
			String displayName = setting.entry.getDisplayName();
			if (displayName.toLowerCase(Locale.ROOT).contains(searchText) || setting.entry.getName().toLowerCase(Locale.ROOT).contains(searchText)) {
				rows.add(new DuskConfigRow(displayName, setting.widgets));
			}
		}
		list.setRows(rows);
	}

	public void onSettingChanged() {
		if (doneButton == null) {
			return;
		}

		doneButton.active = true;
		for (DuskConfigSetting setting : settings) {
			setting.updateResetButton();
			if (!setting.isValid) {
				doneButton.active = false;
			}
		}
	}

	private void saveAndClose() {
		DuskConfig.write(configMod.modid);
		this.minecraft.setScreenAndShow(parent);
	}

	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && searchBox.isMouseOver(event.x(), event.y())) {
			searchBox.setValue("");
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public void onClose() {
		DuskConfigFile.reload(configMod);
		this.minecraft.setScreenAndShow(parent);
	}

	@Override
	public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

		list.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
	}
}
