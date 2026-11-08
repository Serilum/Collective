package com.natamus.collective.config.screen;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.config.DuskConfigCustomFile;
import com.natamus.collective.config.DuskConfigCustomFileEntry;
import com.natamus.collective.config.DuskConfigEntry;
import com.natamus.collective.config.DuskConfigFile;
import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.config.DuskConfigServerConfig;
import com.natamus.collective.networking.packets.ConfigServerClientHandler;
import com.natamus.collective.networking.packets.ConfigSyncClientHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DuskConfigEditScreen extends DuskConfigBaseScreen {
	public static boolean showTooltips = false;

	private final Screen parent;
	private final DuskConfigMod configMod;
	private final boolean isServerConfig;
	private final List<DuskConfigCustomFileEntry> fileEntries = new ArrayList<>();
	private final List<DuskConfigSetting> settings = new ArrayList<>();
	private final List<DuskConfigSetting> fileSettings = new ArrayList<>();
	private final List<String> initialValues = new ArrayList<>();
	private DuskConfigList list;
	private EditBox searchBox;
	private Button doneButton;
	private Tab filesTab;
	private String settingsSearch = "";
	private String filesSearch = "";
	private boolean showFiles = false;

	public DuskConfigEditScreen(Screen parent, DuskConfigMod configMod) {
		this(parent, configMod, null);
	}

	public DuskConfigEditScreen(Screen parent, DuskConfigMod configMod, JsonObject serverConfig) {
		super(getConfigTitle(configMod, serverConfig != null));
		this.parent = parent;
		this.configMod = configMod;
		this.isServerConfig = serverConfig != null;

		if (isServerConfig) {
			useServerConfig(serverConfig);
		}
		else {
			for (DuskConfigCustomFile customFile : DuskConfigCustomFile.getCustomFiles(configMod.modid)) {
				fileEntries.add(new DuskConfigCustomFileEntry(customFile));
			}
		}

		for (DuskConfigEntry entry : configMod.entries) {
			initialValues.add(entry.getText());
		}

		showFiles = configMod.isListOnly && !fileEntries.isEmpty();
	}

	private static Component getConfigTitle(DuskConfigMod configMod, boolean isServerConfig) {
		String modName = StringUtils.capitalize(configMod.name);
		if (isServerConfig) {
			return Component.translatable("collective.collective.gui.serverconfigtitle", modName);
		}
		return Component.translatable("collective.collective.gui.configtitle", modName);
	}

	private void useServerConfig(JsonObject serverConfig) {
		DuskConfigServerConfig.useServerSettings(configMod, serverConfig.getAsJsonObject("settings"));

		JsonObject serverFiles = serverConfig.getAsJsonObject("files");
		for (String key : serverFiles.keySet()) {
			DuskConfigCustomFile customFile = DuskConfigCustomFile.getCustomFile(configMod.modid, key);
			if (customFile != null) {
				fileEntries.add(new DuskConfigCustomFileEntry(customFile, serverFiles.get(key).getAsString()));
			}
		}
	}

	@Override
	protected boolean hasUnsavedChanges() {
		for (int i = 0; i < configMod.entries.size(); i++) {
			if (!configMod.entries.get(i).getText().equals(initialValues.get(i))) {
				return true;
			}
		}
		for (DuskConfigCustomFileEntry fileEntry : fileEntries) {
			if (!fileEntry.isDefault()) {
				return true;
			}
		}
		return false;
	}

	public static Screen getScreen(Screen parent, String modid) {
		if (!DuskConfigModToggles.hasConfigScreen(modid)) {
			return parent;
		}

		DuskConfigMod configMod = prepareConfigMod(modid);
		if (!ConfigServerClientHandler.canEditServerConfig()) {
			return new DuskConfigEditScreen(parent, configMod);
		}

		return getServerOrClientPrompt(parent, configMod);
	}

	private static Screen getServerOrClientPrompt(Screen parent, DuskConfigMod configMod) {
		Component title = getConfigTitle(configMod, false);
		Component question = Component.translatable("collective.collective.gui.servereditquestion");
		Component serverButton = Component.translatable("collective.collective.gui.serverconfig");
		Component clientButton = Component.translatable("collective.collective.gui.clientconfig");

		return new ConfirmScreen(editServerConfig -> {
			if (editServerConfig) {
				ConfigServerClientHandler.requestServerConfig(parent, configMod.modid);
			}
			else {
				Minecraft.getInstance().setScreen(new DuskConfigEditScreen(parent, configMod));
			}
		}, title, question, serverButton, clientButton);
	}

	public static DuskConfigMod prepareConfigMod(String modid) {
		ConfigSyncClientHandler.restoreOwnValuesAfterLeavingServer();

		DuskConfigMod configMod = DuskConfigMod.addListOnlyMod(modid);
		DuskConfigFile.reload(configMod);
		return configMod;
	}

	@Override
	protected void init() {
		super.init();

		settings.clear();
		for (DuskConfigEntry entry : getShownEntries()) {
			settings.add(new DuskConfigSetting(this, entry, this.width - 3));
		}

		fileSettings.clear();
		for (DuskConfigCustomFileEntry fileEntry : fileEntries) {
			fileSettings.add(new DuskConfigSetting(this, fileEntry, this.width - 3));
		}

		searchBox = this.addRenderableWidget(new EditBox(this.font, this.width / 2 - 70, 28, 140, 20, Component.translatable("gui.recipebook.search_hint")));
		searchBox.setHint(Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.DARK_GRAY));
		searchBox.setValue(getSearch());
		searchBox.setResponder(text -> {
			setSearch(text);
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

		if (!isServerConfig) {
			Component folderLabel = Component.translatable("collective.collective.gui.folder");
			int folderRight = tooltipButton.getX() - 4;
			int folderWidth = getFolderButtonWidth(folderLabel, folderRight);
			Button folderButton = this.addRenderableWidget(new DuskConfigButton(folderRight - folderWidth, 28, folderWidth, 20, folderLabel, button -> Util.getPlatform().openPath(configMod.path.getParent())));
			folderButton.setTooltip(Tooltip.create(Component.translatable("collective.collective.gui.foldertooltip")));
		}

		Component patreonLabel = Component.literal("❤ ").withStyle(ChatFormatting.RED).append(Component.literal("Patreon").withStyle(ChatFormatting.WHITE));
		this.addRenderableWidget(new DuskConfigButton(10, 28, this.font.width(patreonLabel) + 12, 20, patreonLabel, ConfirmLinkScreen.confirmLink(this, "https://link.serilum.com/patreon", true)));

		double scroll = 0;
		if (list != null) {
			scroll = list.getScrollAmount();
		}

		list = new DuskConfigList(this.minecraft, this.width, this.height - 92, 56, 25);
		this.addWidget(list);
		refreshRows();
		list.setScrollAmount(scroll);

		if (hasTabs()) {
			addTabBar();
		}

		addCancelButton(this.width / 2 - 154, this.height - 28);
		doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> saveAndClose()).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build());

		onSettingChanged();
	}

	private List<DuskConfigEntry> getShownEntries() {
		List<DuskConfigEntry> shownEntries = new ArrayList<>(configMod.getActiveEntries());
		if (isServerConfig) {
			shownEntries.remove(configMod.enableEntry);
		}
		return shownEntries;
	}

	private boolean hasTabs() {
		return !fileEntries.isEmpty();
	}

	private void addTabBar() {
		Tab settingsTab = new GridLayoutTab(this.title);
		filesTab = new GridLayoutTab(Component.translatable("collective.collective.gui.extraconfigfiles"));

		TabManager tabManager = new DuskConfigTabManager(this::addRenderableWidget, this::removeWidget, this::onTabSelected);
		TabNavigationBar tabBar = TabNavigationBar.builder(tabManager, this.width).addTabs(settingsTab, filesTab).build();
		tabBar.arrangeElements();
		this.addRenderableWidget(tabBar);

		if (showFiles) {
			tabBar.selectTab(1, false);
		}
		else {
			tabBar.selectTab(0, false);
		}
	}

	private void onTabSelected(Tab tab) {
		boolean filesSelected = tab == filesTab;
		if (filesSelected == showFiles) {
			return;
		}

		showFiles = filesSelected;
		searchBox.setValue(getSearch());
		refreshRows();
		list.setScrollAmount(0);
	}

	private int getFolderButtonWidth(Component label, int rightEdge) {
		int spaceAfterSearchBox = rightEdge - (searchBox.getX() + searchBox.getWidth() + 4);
		return Math.min(this.font.width(label) + 12, Math.max(20, spaceAfterSearchBox));
	}

	private List<DuskConfigSetting> getShownSettings() {
		if (showFiles) {
			return fileSettings;
		}
		return settings;
	}

	private String getSearch() {
		if (showFiles) {
			return filesSearch;
		}
		return settingsSearch;
	}

	private void setSearch(String text) {
		if (showFiles) {
			filesSearch = text;
		}
		else {
			settingsSearch = text;
		}
	}

	private List<DuskConfigSetting> getAllSettings() {
		List<DuskConfigSetting> allSettings = new ArrayList<>(settings);
		allSettings.addAll(fileSettings);
		return allSettings;
	}

	private void refreshRows() {
		String searchText = getSearch().strip().toLowerCase(Locale.ROOT);

		List<DuskConfigRow> rows = new ArrayList<>();
		for (DuskConfigSetting setting : getShownSettings()) {
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
		for (DuskConfigSetting setting : getAllSettings()) {
			setting.updateResetButton();
			if (!setting.isValid) {
				doneButton.active = false;
			}
		}
	}

	private void saveAndClose() {
		if (isServerConfig) {
			sendChangesToServer();
			ConfigServerClientHandler.stopEditingServerConfig();
		}
		else {
			DuskConfig.write(configMod.modid);
			saveChangedFiles();
		}
		this.minecraft.setScreen(parent);
	}

	private void saveChangedFiles() {
		MinecraftServer integratedServer = this.minecraft.getSingleplayerServer();
		for (DuskConfigCustomFileEntry fileEntry : fileEntries) {
			if (fileEntry.isDefault()) {
				continue;
			}

			if (integratedServer != null) {
				integratedServer.execute(() -> fileEntry.save(integratedServer));
			}
			else {
				fileEntry.save(null);
			}
		}
	}

	private void sendChangesToServer() {
		JsonObject changedSettings = new JsonObject();
		for (int i = 0; i < configMod.entries.size(); i++) {
			DuskConfigEntry entry = configMod.entries.get(i);
			if (entry != configMod.enableEntry && !entry.getText().equals(initialValues.get(i))) {
				changedSettings.add(entry.getName(), DuskConfigServerConfig.getJsonValue(entry));
			}
		}

		JsonObject changedFiles = new JsonObject();
		for (DuskConfigCustomFileEntry fileEntry : fileEntries) {
			if (!fileEntry.isDefault()) {
				changedFiles.addProperty(fileEntry.customFile.getKey(), fileEntry.getText());
			}
		}

		if (changedSettings.size() > 0 || changedFiles.size() > 0) {
			ConfigServerClientHandler.sendChanges(configMod.modid, changedSettings, changedFiles);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		List<DuskConfigSetting> settingsPendingReset = getSettingsPendingReset();

		boolean handled;
		if (button == InputConstants.MOUSE_BUTTON_RIGHT && searchBox.isMouseOver(mouseX, mouseY)) {
			searchBox.setValue("");
			handled = true;
		}
		else {
			handled = super.mouseClicked(mouseX, mouseY, button);
		}

		for (DuskConfigSetting setting : settingsPendingReset) {
			setting.cancelPendingReset();
		}
		return handled;
	}

	private List<DuskConfigSetting> getSettingsPendingReset() {
		List<DuskConfigSetting> settingsPendingReset = new ArrayList<>();
		for (DuskConfigSetting setting : getAllSettings()) {
			if (setting.isResetPending()) {
				settingsPendingReset.add(setting);
			}
		}
		return settingsPendingReset;
	}

	@Override
	public void onClose() {
		if (isServerConfig) {
			ConfigServerClientHandler.stopEditingServerConfig();
		}
		else {
			DuskConfigFile.reload(configMod);
		}
		this.minecraft.setScreen(parent);
	}

	@Override
	public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (this.minecraft.level == null) {
			this.renderPanorama(guiGraphics, partialTick);
		}

		this.renderBlurredBackground(partialTick);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		list.render(guiGraphics, mouseX, mouseY, partialTick);
		if (!hasTabs()) {
			guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
		}
	}
}
