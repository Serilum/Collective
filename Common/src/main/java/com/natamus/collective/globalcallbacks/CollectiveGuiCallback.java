package com.natamus.collective.globalcallbacks;

import com.natamus.collective.implementations.event.Event;
import com.natamus.collective.implementations.event.EventFactory;
import net.minecraft.client.gui.GuiGraphics;

public class CollectiveGuiCallback {
	private CollectiveGuiCallback() { }

    public static final Event<On_Gui_Render> ON_GUI_RENDER = EventFactory.createArrayBacked(On_Gui_Render.class, callbacks -> (guiGraphics, tickDelta) -> {
        for (On_Gui_Render callback : callbacks) {
        	callback.onGuiRender(guiGraphics, tickDelta);
        }
    });

	@FunctionalInterface
	public interface On_Gui_Render {
		 void onGuiRender(GuiGraphics guiGraphics, float tickDelta);
	}
}
