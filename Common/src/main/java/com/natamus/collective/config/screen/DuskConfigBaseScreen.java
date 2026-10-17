package com.natamus.collective.config.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.config.DuskConfigFile;
import com.natamus.collective.config.DuskConfigMod;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class DuskConfigBaseScreen extends Screen {
	private Button cancelButton;
	private boolean cancelPending = false;
	private static final int smallestGuiScale = 2;

	protected DuskConfigBaseScreen(Component title) {
		super(title);
	}

	protected abstract boolean hasUnsavedChanges();

	protected void addCancelButton(int x, int y) {
		cancelPending = false;
		cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> pressCancel()).pos(x, y).size(150, 20).build());
	}

	private void pressCancel() {
		if (cancelPending || !hasUnsavedChanges()) {
			onClose();
			return;
		}

		cancelPending = true;
		cancelButton.setMessage(Component.translatable("collective.collective.gui.discardchanges").withStyle(ChatFormatting.RED));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == InputConstants.KEY_ESCAPE && this.shouldCloseOnEsc()) {
			pressCancel();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean pendingBeforeClick = cancelPending;
		boolean handled = super.mouseClicked(mouseX, mouseY, button);

		if (pendingBeforeClick && cancelPending) {
			cancelPending = false;
			cancelButton.setMessage(CommonComponents.GUI_CANCEL);
		}
		return handled;
	}

	@Override
	public void added() {
		super.added();
		applyGuiScale();
	}

	@Override
	public void removed() {
		super.removed();
		Minecraft.getInstance().getWindow().setGuiScale(getGameGuiScale());
	}

	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		applyGuiScale();

		Window window = minecraft.getWindow();
		super.resize(minecraft, window.getGuiScaledWidth(), window.getGuiScaledHeight());
	}

	protected void cycleGuiScale(boolean smaller) {
		int guiScale = getScreenGuiScale();
		if (smaller) {
			guiScale -= 1;
		}
		else {
			guiScale += 1;
		}

		int maxGuiScale = getMaxGuiScale();
		if (guiScale < smallestGuiScale) {
			guiScale = maxGuiScale;
		}
		else if (guiScale > maxGuiScale) {
			guiScale = smallestGuiScale;
		}

		if (guiScale == getGameGuiScale()) {
			guiScale = 0;
		}

		CollectiveConfigHandler.configScreenGuiScale = guiScale;
		DuskConfigMod collectiveConfig = DuskConfigMod.configMods.get(CollectiveReference.MOD_ID);
		DuskConfigFile.writeValue(collectiveConfig, collectiveConfig.getEntry("configScreenGuiScale"));
		this.resize(Minecraft.getInstance(), this.width, this.height);
	}

	protected Component getGuiScaleLabel() {
		if (CollectiveConfigHandler.configScreenGuiScale <= 0) {
			return Component.translatable("options.guiScale.auto");
		}
		return Component.literal(String.valueOf(getScreenGuiScale()));
	}

	private void applyGuiScale() {
		Minecraft.getInstance().getWindow().setGuiScale(getScreenGuiScale());
	}

	private int getScreenGuiScale() {
		int guiScale = CollectiveConfigHandler.configScreenGuiScale;
		if (guiScale <= 0) {
			return getGameGuiScale();
		}
		return Math.max(smallestGuiScale, Math.min(guiScale, getMaxGuiScale()));
	}

	private int getGameGuiScale() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.getWindow().calculateScale(minecraft.options.guiScale().get(), minecraft.isEnforceUnicode());
	}

	private int getMaxGuiScale() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode()) + 1;
	}
}
