package com.natamus.collective.fabric.mixin;

import com.natamus.collective.worldcreation.WorldCreationTabLayout;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GridLayoutTab.class, priority = 1001)
public class GridLayoutTabMixin implements WorldCreationTabLayout {
	@Shadow @Final protected GridLayout layout;

	@Override
	public GridLayout collective$getLayout() {
		return layout;
	}
}
