package com.natamus.collective.config.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.natamus.collective.config.DuskConfigEntry;
import com.natamus.collective.functions.ConfigFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DuskConfigRegistryScreen extends DuskConfigBaseScreen {
	private final Screen parent;
	private final DuskConfigEntry entry;
	private final List<String> selectedIds = new ArrayList<>();
	private final List<String> initialIds;
	private final List<String> registryIds;
	private final List<String> suggestedIds;
	private final HashMap<String, ItemStack> icons = new HashMap<>();
	private final HashMap<String, String> names = new HashMap<>();
	private final HashMap<String, Boolean> loadedIds = new HashMap<>();
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
		this.registryIds = DuskConfigRegistryIds.getIds(entry.registry);
		this.suggestedIds = DuskConfigRegistryIds.getSuggestedIds(entry.registry, entry.filter);
		this.showAll = suggestedIds.isEmpty();
	}

	@Override
	protected void init() {
		super.init();

		int listWidth = this.width / 2 - 15;
		int listHeight = this.height - 122;

		double selectedScroll = 0;
		double availableScroll = 0;
		if (selectedList != null) {
			selectedScroll = selectedList.getScrollAmount();
			availableScroll = availableList.getScrollAmount();
		}

		selectedList = this.addWidget(new DuskConfigIdList(this.minecraft, 10, 58, listWidth, listHeight));
		availableList = this.addWidget(new DuskConfigIdList(this.minecraft, this.width / 2 + 5, 58, listWidth, listHeight));

		int searchWidth = listWidth;
		if (!entry.filter.isEmpty()) {
			searchWidth = listWidth - 84;
			addFilterButton(this.width / 2 + 5 + searchWidth + 4);
		}

		searchBox = this.addRenderableWidget(new EditBox(this.font, this.width / 2 + 5, 34, searchWidth, 20, Component.translatable("gui.recipebook.search_hint")));
		searchBox.setHint(Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.DARK_GRAY));
		searchBox.setValue(search);
		searchBox.setResponder(text -> {
			search = text;
			refreshAvailableList();
			availableList.setScrollAmount(0);
		});

		EditBox patternBox = this.addRenderableWidget(new EditBox(this.font, 10, this.height - 54, listWidth - 65, 20, Component.translatable("collective.collective.gui.idpattern")));
		patternBox.setMaxLength(Integer.MAX_VALUE);
		patternBox.setHint(Component.translatable("collective.collective.gui.idpattern").withStyle(ChatFormatting.DARK_GRAY));
		this.addRenderableWidget(new DuskConfigButton(listWidth - 50, this.height - 54, 60, 20, Component.translatable("collective.collective.gui.add"), button -> {
			addId(patternBox.getValue().strip());
			patternBox.setValue("");
		}));

		addCancelButton(this.width / 2 - 154, this.height - 28);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			entry.setIdList(selectedIds);
			onClose();
		}).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build());

		refreshLists();
		selectedList.setScrollAmount(selectedScroll);
		availableList.setScrollAmount(availableScroll);
	}

	@Override
	protected boolean hasUnsavedChanges() {
		return !selectedIds.equals(initialIds);
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
		return DuskConfigRegistryIds.getFilterLabel(entry.filter);
	}

	private void refreshLists() {
		refreshSelectedList();
		refreshAvailableList();
	}

	private void refreshSelectedList() {
		List<DuskConfigIdRow> selectedRows = new ArrayList<>();
		for (String id : selectedIds) {
			int colour = 0xFF808080;
			if (isLoaded(id)) {
				colour = 0xFFFFFFFF;
			}
			selectedRows.add(new DuskConfigIdRow(getIcon(id), getName(id), id, colour, getRemoveLabel(id), button -> pressRemove(id)));
		}
		selectedList.setRows(selectedRows);
	}

	private void refreshAvailableList() {
		String search = searchBox.getValue().strip().toLowerCase(Locale.ROOT);

		List<String> shownIds = suggestedIds;
		if (showAll) {
			shownIds = registryIds;
		}

		List<DuskConfigIdRow> availableRows = new ArrayList<>();
		for (String id : shownIds) {
			if (!isSelected(id) && (id.contains(search) || getName(id).toLowerCase(Locale.ROOT).contains(search))) {
				availableRows.add(new DuskConfigIdRow(getIcon(id), getName(id), id, 0xFFFFFFFF, Component.literal("+").withStyle(ChatFormatting.GREEN), button -> addId(id)));
			}
		}
		availableList.setRows(availableRows);
	}

	private boolean isSelected(String id) {
		for (String selectedId : selectedIds) {
			if (selectedId.equalsIgnoreCase(id)) {
				return true;
			}
		}
		return false;
	}

	private boolean isLoaded(String id) {
		return loadedIds.computeIfAbsent(id, loadedId -> {
			for (String registryId : registryIds) {
				if (ConfigFunctions.isInIdList(loadedId, registryId)) {
					return true;
				}
			}
			return false;
		});
	}

	private ItemStack getIcon(String id) {
		return icons.computeIfAbsent(id, iconId -> DuskConfigRegistryIds.getIcon(entry.registry, iconId));
	}

	private String getName(String id) {
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

		selectedIds.add(id);
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
		selectedIds.removeIf(selectedId -> selectedId.equalsIgnoreCase(id));
		refreshLists();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		String pendingBeforeClick = pendingRemovalId;

		boolean handled;
		if (button == InputConstants.MOUSE_BUTTON_RIGHT && searchBox.isMouseOver(mouseX, mouseY)) {
			searchBox.setValue("");
			handled = true;
		}
		else {
			handled = super.mouseClicked(mouseX, mouseY, button);
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
	public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (this.minecraft.level == null) {
			this.renderPanorama(guiGraphics, partialTick);
		}
		this.renderBlurredBackground(partialTick);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		selectedList.render(guiGraphics, mouseX, mouseY, partialTick);
		availableList.render(guiGraphics, mouseX, mouseY, partialTick);

		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
		guiGraphics.drawString(this.font, Component.translatable("pack.selected.title").append(" (" + selectedIds.size() + ")"), 12, 40, 0xFFA0A0A0);
		if (registryIds.isEmpty()) {
			guiGraphics.drawString(this.font, Component.translatable("collective.collective.gui.worldneeded"), this.width / 2 + 7, 64, 0xFFA0A0A0);
		}
		else if (this.minecraft.level == null && !entry.filter.isEmpty() && suggestedIds.isEmpty()) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(this.width / 2F, 23, 0);
			guiGraphics.pose().scale(0.75F, 0.75F, 1F);
			guiGraphics.drawCenteredString(this.font, Component.translatable("collective.collective.gui.worldneeded"), 0, 0, 0xFFFFAA00);
			guiGraphics.pose().popPose();
		}
	}
}
