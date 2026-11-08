package com.natamus.collective.config.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.natamus.collective.config.DuskConfigCustomFileEntry;
import com.natamus.collective.config.DuskConfigEntry;
import com.natamus.collective.config.DuskConfigModToggles;
import com.natamus.collective.config.DuskConfigValueEntry;
import com.natamus.collective.functions.ConfigFunctions;
import com.natamus.collective.functions.StringFunctions;
import com.natamus.collective.objects.IdAmount;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DuskConfigRegistryScreen extends DuskConfigBaseScreen {
	private static final int whiteChatColourId = 15;

	private final Screen parent;
	private final DuskConfigEntry entry;
	private final List<String> selectedIds = new ArrayList<>();
	private final List<String> initialIds;
	private final List<String> registryIds;
	private final List<String> suggestedIds;
	private final HashMap<String, ItemStack> icons = new HashMap<>();
	private final HashMap<String, String> names = new HashMap<>();
	private final HashMap<String, Boolean> loadedIds = new HashMap<>();
	private final HashMap<String, EditBox> weightBoxes = new HashMap<>();
	private final HashMap<String, String> defaultEntries;
	private final HashMap<String, Button> resetButtons = new HashMap<>();
	private DuskConfigIdList selectedList;
	private DuskConfigIdList availableList;
	private EditBox searchBox;
	private boolean showAll;
	private String pendingRemovalId = null;
	private String search = "";

	public DuskConfigRegistryScreen(Screen parent, DuskConfigEntry entry) {
		super(Component.literal(entry.getDisplayName()));
		this.parent = parent;
		this.entry = entry;
		for (String id : entry.getIdList()) {
			if (!isSelected(id)) {
				selectedIds.add(id);
			}
		}
		this.initialIds = new ArrayList<>(selectedIds);
		this.registryIds = getAllIds();
		this.suggestedIds = getSuggestedIds();
		this.defaultEntries = getDefaultEntries();
		this.showAll = suggestedIds.isEmpty();
	}

	private List<String> getAllIds() {
		List<String> allIds = DuskConfigRegistryIds.getIds(entry.registry);
		for (String variantId : entry.getVariantIds()) {
			if (!allIds.contains(variantId)) {
				allIds.add(variantId);
			}
		}
		return allIds;
	}

	private List<String> getSuggestedIds() {
		if (hasVariantList()) {
			return new ArrayList<>(entry.getVariantIds());
		}
		return DuskConfigRegistryIds.getSuggestedIds(entry.registry, entry.filter);
	}

	private boolean hasVariantList() {
		return !entry.getVariantIds().isEmpty();
	}

	private boolean isFixedList() {
		return entry.isFixedList() && (entry.hasIdNumbers() || entry.hasChatColourValues() || entry.isIdMap());
	}

	private boolean showsVariants() {
		return hasVariantList() || isFixedList();
	}

	@Override
	protected void init() {
		super.init();

		if (isFixedList()) {
			initFixedList();
		}
		else {
			initPickerLists();
		}

		addCancelButton(this.width / 2 - 154, this.height - 28);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			entry.setIdList(selectedIds);
			onClose();
		}).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build());
	}

	private void initPickerLists() {
		int listWidth = this.width / 2 - 15;
		int listHeight = this.height - 122;

		double selectedScroll = 0;
		double availableScroll = 0;
		if (selectedList != null) {
			selectedScroll = selectedList.scrollAmount();
			availableScroll = availableList.scrollAmount();
		}

		selectedList = this.addWidget(new DuskConfigIdList(this.minecraft, 10, 58, listWidth, listHeight));
		availableList = this.addWidget(new DuskConfigIdList(this.minecraft, this.width / 2 + 5, 58, listWidth, listHeight));

		int searchWidth = listWidth;
		if (!entry.filter.isEmpty() || hasVariantList()) {
			searchWidth = listWidth - 84;
			addFilterButton(this.width / 2 + 5 + searchWidth + 4);
		}
		addSearchBox(this.width / 2 + 5, searchWidth, availableList);

		if (!entry.registry.equals(DuskConfigModToggles.modRegistry)) {
			addPatternRow(listWidth);
		}

		refreshLists();
		selectedList.setScrollAmount(selectedScroll);
		availableList.setScrollAmount(availableScroll);
	}

	private void initFixedList() {
		int listWidth = this.width - 20;
		int listHeight = this.height - 98;

		double selectedScroll = 0;
		if (selectedList != null) {
			selectedScroll = selectedList.scrollAmount();
		}

		selectedList = this.addWidget(new DuskConfigIdList(this.minecraft, 10, 58, listWidth, listHeight));
		addSearchBox(this.width - 210, 200, selectedList);

		refreshLists();
		selectedList.setScrollAmount(selectedScroll);
	}

	private void addSearchBox(int x, int width, DuskConfigIdList searchedList) {
		searchBox = this.addRenderableWidget(new EditBox(this.font, x, 34, width, 20, Component.translatable("gui.recipebook.search_hint")));
		searchBox.setHint(Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.DARK_GRAY));
		searchBox.setValue(search);
		searchBox.setResponder(text -> {
			search = text;
			if (searchedList == selectedList) {
				refreshSelectedList();
			}
			else {
				refreshAvailableList();
			}
			searchedList.setScrollAmount(0);
		});
	}

	@Override
	protected boolean hasUnsavedChanges() {
		return !selectedIds.equals(initialIds);
	}

	private void addPatternRow(int listWidth) {
		EditBox patternBox = this.addRenderableWidget(new EditBox(this.font, 10, this.height - 54, listWidth - 65, 20, Component.translatable("collective.collective.gui.idpattern")));
		patternBox.setMaxLength(Integer.MAX_VALUE);
		patternBox.setHint(getHintThatFits(patternBox, Component.translatable("collective.collective.gui.idpattern").withStyle(ChatFormatting.DARK_GRAY)));
		this.addRenderableWidget(new DuskConfigButton(listWidth - 50, this.height - 54, 60, 20, Component.translatable("collective.collective.gui.add"), button -> {
			addId(patternBox.getValue().strip());
			patternBox.setValue("");
		}));
	}

	private void addFilterButton(int x) {
		Button filterButton = this.addRenderableWidget(new DuskConfigButton(x, 34, 80, 20, getFilterLabel(), this::toggleFilter, this::toggleFilter));

		if (suggestedIds.isEmpty()) {
			filterButton.active = false;
			filterButton.setTooltip(Tooltip.create(Component.translatable("collective.collective.gui.worldneeded")));
		}
	}

	private void toggleFilter(Button button) {
		showAll = !showAll;
		button.setMessage(getFilterLabel());
		refreshAvailableList();
		availableList.setScrollAmount(0);
	}

	private Component getFilterLabel() {
		if (showAll) {
			return DuskConfigRegistryIds.getAllLabel(entry.registry);
		}
		if (hasVariantList()) {
			return DuskConfigRegistryIds.getSuggestedLabel();
		}
		return DuskConfigRegistryIds.getFilterLabel(entry.filter);
	}

	private void refreshLists() {
		refreshSelectedList();
		refreshAvailableList();
	}

	private void refreshSelectedList() {
		weightBoxes.clear();
		resetButtons.clear();

		List<DuskConfigIdRow> selectedRows = new ArrayList<>();
		for (String selectedEntry : getShownSelectedEntries()) {
			String id = getEntryId(selectedEntry);
			int colour = 0xFF808080;
			if (isLoaded(id)) {
				colour = 0xFFFFFFFF;
			}

			AbstractWidget valueWidget = createValueWidget(id, selectedEntry);
			Button resetButton = createResetButton(id, selectedEntry);
			if (isFixedList()) {
				selectedRows.add(new DuskConfigIdRow(getIcon(id), getModIcon(id), getEffectSprite(id), getName(id), id, colour, valueWidget, resetButton));
			}
			else {
				selectedRows.add(new DuskConfigIdRow(getIcon(id), getModIcon(id), getEffectSprite(id), getName(id), id, colour, getRemoveLabel(id), button -> pressRemove(id), valueWidget, resetButton));
			}
		}
		selectedList.setRows(selectedRows);

		refreshWeightTooltips();
	}

	private Button createResetButton(String id, String selectedEntry) {
		String defaultEntry = defaultEntries.get(id.toLowerCase(Locale.ROOT));
		if (defaultEntry == null) {
			return null;
		}

		Button resetButton = new DuskConfigButton(0, 0, 40, 20, Component.translatable("controls.reset").withStyle(ChatFormatting.RED), button -> resetToDefault(id, defaultEntry));
		resetButton.setTooltip(Tooltip.create(Component.translatable("collective.collective.message.configdefault", getDefaultValueText(defaultEntry))));
		resetButton.active = !isDefaultEntry(selectedEntry, defaultEntry);
		resetButtons.put(id.toLowerCase(Locale.ROOT), resetButton);
		return resetButton;
	}

	private boolean isDefaultEntry(String selectedEntry, String defaultEntry) {
		return selectedEntry.replace(" ", "").equalsIgnoreCase(defaultEntry.replace(" ", ""));
	}

	private void updateResetButton(String id) {
		Button resetButton = resetButtons.get(id.toLowerCase(Locale.ROOT));
		if (resetButton == null) {
			return;
		}

		String defaultEntry = defaultEntries.get(id.toLowerCase(Locale.ROOT));
		for (String selectedEntry : selectedIds) {
			if (getEntryId(selectedEntry).equalsIgnoreCase(id)) {
				resetButton.active = !isDefaultEntry(selectedEntry, defaultEntry);
			}
		}
	}

	private String getDefaultValueText(String defaultEntry) {
		if (entry.isIdMap()) {
			return getMapValues(defaultEntry);
		}
		return getAmountText(defaultEntry);
	}

	private void resetToDefault(String id, String defaultEntry) {
		for (int i = 0; i < selectedIds.size(); i++) {
			if (getEntryId(selectedIds.get(i)).equalsIgnoreCase(id)) {
				selectedIds.set(i, defaultEntry);
			}
		}
		refreshSelectedList();
	}

	private HashMap<String, String> getDefaultEntries() {
		HashMap<String, String> entriesById = new HashMap<>();
		for (String defaultEntry : entry.getDefaultIdList()) {
			entriesById.put(getEntryId(defaultEntry).toLowerCase(Locale.ROOT), defaultEntry);
		}
		return entriesById;
	}

	private List<String> getShownSelectedEntries() {
		List<String> shownEntries = new ArrayList<>();
		for (String selectedEntry : selectedIds) {
			if (!isFixedList() || matchesSearch(getEntryId(selectedEntry))) {
				shownEntries.add(selectedEntry);
			}
		}

		if (showsVariants()) {
			shownEntries.sort(Comparator.comparing(selectedEntry -> getSortName(getEntryId(selectedEntry))));
		}
		return shownEntries;
	}

	private boolean matchesSearch(String id) {
		String searchText = search.strip().toLowerCase(Locale.ROOT);
		return id.contains(searchText) || getName(id).toLowerCase(Locale.ROOT).contains(searchText);
	}

	private String getSortName(String id) {
		if (getName(id).isEmpty()) {
			return id.toLowerCase(Locale.ROOT);
		}
		return getName(id).toLowerCase(Locale.ROOT);
	}

	private AbstractWidget createValueWidget(String id, String selectedEntry) {
		if (entry instanceof DuskConfigCustomFileEntry fileEntry && fileEntry.isIdMap()) {
			return createMapValuesButton(fileEntry, id, selectedEntry);
		}
		if (entry.hasChatColourValues()) {
			return createChatColourButton(id, selectedEntry);
		}
		if (entry.hasIdNumbers()) {
			return createAmountBox(id, selectedEntry);
		}
		return null;
	}

	private Button createChatColourButton(String id, String selectedEntry) {
		int colourId = getChatColourId(selectedEntry);
		Component title = Component.literal(getDisplayName(id));
		return new DuskConfigButton(0, 0, 80, 20, DuskConfigChatColourScreen.getColourLabel(colourId), button -> {
			this.minecraft.setScreen(new DuskConfigChatColourScreen(this, title, colourId, pickedColourId -> setAmount(id, String.valueOf(pickedColourId))));
		});
	}

	private int getChatColourId(String selectedEntry) {
		try {
			return Integer.parseInt(getAmountText(selectedEntry));
		}
		catch (NumberFormatException ignored) {
			return whiteChatColourId;
		}
	}

	private Button createMapValuesButton(DuskConfigCustomFileEntry fileEntry, String key, String selectedEntry) {
		DuskConfigValueEntry valueEntry = fileEntry.getMapValueEntry(getDisplayName(key), getMapValues(selectedEntry), values -> setMapValues(key, values));
		return new DuskConfigButton(0, 0, 80, 20, getMapValuesLabel(valueEntry), button -> this.minecraft.setScreen(new DuskConfigRegistryScreen(this, valueEntry)));
	}

	private Component getMapValuesLabel(DuskConfigValueEntry valueEntry) {
		List<String> values = valueEntry.getIdList();
		if (values.isEmpty()) {
			return Component.translatable("gui.none").withStyle(ChatFormatting.GRAY);
		}
		if (valueEntry.single) {
			return Component.literal(DuskConfigRegistryIds.getName(valueEntry.registry, IdAmount.getId(values.get(0))));
		}
		return DuskConfigRegistryIds.getAmountLabel(valueEntry.registry, values.size());
	}

	private String getMapValues(String selectedEntry) {
		return StringFunctions.getTextAfter(selectedEntry, ";");
	}

	private void setMapValues(String key, String values) {
		for (int i = 0; i < selectedIds.size(); i++) {
			if (getEntryId(selectedIds.get(i)).equalsIgnoreCase(key)) {
				selectedIds.set(i, key + ";" + values);
				return;
			}
		}
	}

	private String getDisplayName(String id) {
		if (getName(id).isEmpty()) {
			return id;
		}
		return getName(id);
	}

	private EditBox createAmountBox(String id, String selectedEntry) {
		EditBox amountBox = new EditBox(this.font, 0, 0, 44, 20, getAmountBoxLabel());
		amountBox.setValue(getAmountText(selectedEntry));
		if (entry.weights) {
			amountBox.setHint(getHintThatFits(amountBox, getAmountBoxLabel()));
			weightBoxes.put(id, amountBox);
		}
		else if (entry.hasLevelDurationValues()) {
			amountBox.setTooltip(Tooltip.create(getAmountBoxLabel()));
		}

		amountBox.setResponder(text -> {
			if (!isValidAmountBoxText(text.strip())) {
				amountBox.setTextColor(0xFFFF0000);
				return;
			}

			amountBox.setTextColor(0xFFE0E0E0);
			setAmount(id, text.strip());
			refreshWeightTooltips();
		});
		return amountBox;
	}

	private String getAmountText(String selectedEntry) {
		if (entry.idValues) {
			if (!selectedEntry.contains("=")) {
				return "1";
			}
			return StringFunctions.getTextAfterLast(selectedEntry, "=").strip();
		}
		return new IdAmount(selectedEntry).getAmountText();
	}

	private Component getHintThatFits(EditBox editBox, Component hint) {
		String fittedHint = DuskConfigRow.fitLabel(this.font, hint.getString(), editBox.getInnerWidth());
		return Component.literal(fittedHint).withStyle(hint.getStyle());
	}

	private Component getAmountBoxLabel() {
		if (entry.weights) {
			return Component.translatable("collective.collective.gui.weight");
		}
		if (entry.hasLevelDurationValues()) {
			return Component.translatable("collective.collective.gui.levelseconds");
		}
		if (entry.idValues) {
			return Component.translatable("collective.collective.gui.value");
		}
		return Component.translatable("collective.collective.gui.amount");
	}

	private boolean isValidAmountBoxText(String text) {
		if (entry.weights) {
			return IdAmount.isValidWeightText(text);
		}
		if (entry.hasLevelDurationValues()) {
			return text.matches("\\d{1,9}(:\\d{1,9})?");
		}
		if (entry.idValues) {
			return text.matches("-?\\d{1,9}(\\.\\d{1,9})?");
		}
		return IdAmount.isValidAmountText(text);
	}

	private void refreshWeightTooltips() {
		if (weightBoxes.isEmpty()) {
			return;
		}

		int totalWeight = 0;
		for (String selectedEntry : selectedIds) {
			totalWeight += new IdAmount(selectedEntry).maximum;
		}

		for (String selectedEntry : selectedIds) {
			EditBox weightBox = weightBoxes.get(getEntryId(selectedEntry));
			if (weightBox == null) {
				continue;
			}

			double share = 0;
			if (totalWeight > 0) {
				share = new IdAmount(selectedEntry).maximum * 100.0 / totalWeight;
			}
			weightBox.setTooltip(Tooltip.create(Component.translatable("collective.collective.gui.weightshare", String.format(Locale.ROOT, "%.1f", share))));
		}
	}

	private void setAmount(String id, String amountText) {
		for (int i = 0; i < selectedIds.size(); i++) {
			if (getEntryId(selectedIds.get(i)).equalsIgnoreCase(id)) {
				selectedIds.set(i, getAmountEntry(id, amountText));
				updateResetButton(id);
				return;
			}
		}
	}

	private String getAmountEntry(String id, String amountText) {
		if (entry.idValues) {
			return id + "=" + amountText;
		}
		return new IdAmount(id + "=" + amountText).toEntry();
	}

	private String getEntryId(String selectedEntry) {
		if (entry.isIdMap()) {
			return StringFunctions.getTextBefore(selectedEntry, ";").strip();
		}
		if (!entry.hasIdNumbers()) {
			return selectedEntry;
		}
		return IdAmount.getId(selectedEntry);
	}

	private void refreshAvailableList() {
		if (availableList == null) {
			return;
		}

		List<String> shownIds = new ArrayList<>(suggestedIds);
		if (showAll) {
			shownIds = new ArrayList<>(registryIds);
		}
		if (showsVariants()) {
			shownIds.sort(Comparator.comparing(this::getSortName));
		}

		Set<String> selectedEntryIds = getSelectedEntryIds();
		List<DuskConfigIdRow> availableRows = new ArrayList<>();
		for (String id : shownIds) {
			if (!selectedEntryIds.contains(id.toLowerCase(Locale.ROOT)) && matchesSearch(id)) {
				availableRows.add(new DuskConfigIdRow(getIcon(id), getModIcon(id), getEffectSprite(id), getName(id), id, 0xFFFFFFFF, Component.literal("+").withStyle(ChatFormatting.GREEN), button -> addId(id)));
			}
		}
		availableList.setRows(availableRows);
	}

	private Set<String> getSelectedEntryIds() {
		Set<String> selectedEntryIds = new HashSet<>();
		for (String selectedEntry : selectedIds) {
			selectedEntryIds.add(getEntryId(selectedEntry).toLowerCase(Locale.ROOT));
		}
		return selectedEntryIds;
	}

	private boolean isSelected(String id) {
		String comparedId = getEntryId(id);
		for (String selectedEntry : selectedIds) {
			if (getEntryId(selectedEntry).equalsIgnoreCase(comparedId)) {
				return true;
			}
		}
		return false;
	}

	private boolean isLoaded(String id) {
		return loadedIds.computeIfAbsent(id, loadedId -> {
			String checkedId = getMainEntityIdIfVariant(loadedId);
			for (String registryId : registryIds) {
				if (ConfigFunctions.isInIdList(checkedId, registryId)) {
					return true;
				}
			}
			return false;
		});
	}

	private String getMainEntityIdIfVariant(String id) {
		if (!showsVariants()) {
			return id;
		}

		String mainEntityId = DuskConfigRegistryIds.getMainEntityId(entry.registry, id);
		if (mainEntityId == null) {
			return id;
		}
		return mainEntityId;
	}

	private ItemStack getIcon(String id) {
		if (showsVariants()) {
			return icons.computeIfAbsent(id, iconId -> DuskConfigRegistryIds.getIconWithVariants(entry.registry, iconId));
		}
		return icons.computeIfAbsent(id, iconId -> DuskConfigRegistryIds.getIcon(entry.registry, iconId));
	}

	private DuskConfigModIcons getModIcon(String id) {
		if (!entry.registry.equals(DuskConfigModToggles.modRegistry)) {
			return null;
		}
		return DuskConfigModIcons.getIcon(id);
	}

	private Identifier getEffectSprite(String id) {
		return DuskConfigRegistryIds.getEffectSprite(entry.registry, id);
	}

	private String getName(String id) {
		if (showsVariants()) {
			return names.computeIfAbsent(id, nameId -> DuskConfigRegistryIds.getNameWithVariants(entry.registry, nameId));
		}
		return names.computeIfAbsent(id, nameId -> DuskConfigRegistryIds.getName(entry.registry, nameId));
	}

	private void addId(String id) {
		if (id.isEmpty() || isSelected(id)) {
			return;
		}

		if (entry.single) {
			if (id.contains("*")) {
				return;
			}
			selectedIds.clear();
		}

		if (entry.isIdMap()) {
			selectedIds.add(id + ";");
		}
		else if (entry.hasChatColourValues()) {
			selectedIds.add(id + "=" + whiteChatColourId);
		}
		else if (entry.hasIdNumbers()) {
			selectedIds.add(new IdAmount(id).toEntry());
		}
		else {
			selectedIds.add(id);
		}
		refreshLists();
	}

	private Component getRemoveLabel(String id) {
		if (id.equals(pendingRemovalId)) {
			return Component.literal("✔").withStyle(ChatFormatting.RED);
		}
		return Component.literal("-").withStyle(ChatFormatting.RED);
	}

	private void pressRemove(String id) {
		if (id.equals(pendingRemovalId)) {
			pendingRemovalId = null;
			removeId(id);
			return;
		}

		pendingRemovalId = id;
		refreshSelectedList();
	}

	private void removeId(String id) {
		selectedIds.removeIf(selectedEntry -> getEntryId(selectedEntry).equalsIgnoreCase(id));
		refreshLists();
	}

	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean doubleClick) {
		String pendingBeforeClick = pendingRemovalId;

		boolean handled;
		if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && searchBox.isMouseOver(event.x(), event.y())) {
			searchBox.setValue("");
			handled = true;
		}
		else {
			handled = super.mouseClicked(event, doubleClick);
		}

		if (pendingBeforeClick != null && pendingBeforeClick.equals(pendingRemovalId)) {
			pendingRemovalId = null;
			refreshSelectedList();
		}
		return handled;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}

	@Override
	public void extractRenderState(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

		selectedList.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		if (availableList != null) {
			availableList.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		}

		guiGraphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
		guiGraphics.text(this.font, Component.translatable("pack.selected.title").append(" (" + selectedIds.size() + ")"), 12, 40, 0xFFA0A0A0);
		if (!DuskConfigRegistryIds.needsWorld(entry.registry)) {
			return;
		}
		if (registryIds.isEmpty()) {
			extractWorldNeededNotice(guiGraphics);
		}
		else if (this.minecraft.level == null && DuskConfigRegistryIds.worldCreationRegistries == null) {
			guiGraphics.pose().pushMatrix();
			guiGraphics.pose().translate(this.width / 2F, 23);
			guiGraphics.pose().scale(0.75F, 0.75F);
			guiGraphics.centeredText(this.font, getOutOfWorldNotice(), 0, 0, 0xFFFFAA00);
			guiGraphics.pose().popMatrix();
		}
	}

	private Component getOutOfWorldNotice() {
		if (DuskConfigRegistryIds.isBuiltInRegistry(entry.registry)) {
			return Component.translatable("collective.collective.gui.worldneeded");
		}
		return Component.translatable("collective.collective.gui.cachedlist");
	}

	private void extractWorldNeededNotice(GuiGraphicsExtractor guiGraphics) {
		int noticeX = this.width / 2 + 15;
		int noticeY = 70;
		int noticeWidth = this.width / 2 - 35;
		for (FormattedCharSequence noticeLine : this.font.split(Component.translatable("collective.collective.gui.worldneeded"), noticeWidth)) {
			guiGraphics.text(this.font, noticeLine, noticeX, noticeY, 0xFFA0A0A0);
			noticeY += this.font.lineHeight + 2;
		}
	}
}
