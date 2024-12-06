package com.natamus.collective.fabric.callbacks;

import com.mojang.brigadier.ParseResults;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandSourceStack;

public class CollectiveCommandEvents {
	private CollectiveCommandEvents() { }
	 
    public static final Event<On_Command_Parse> ON_COMMAND_PARSE = EventFactory.createArrayBacked(On_Command_Parse.class, callbacks -> (string, parse) -> {
        for (On_Command_Parse callback : callbacks) {
        	callback.onCommandParse(string, parse);
        }
	});
	
	@FunctionalInterface
	public interface On_Command_Parse {
		 void onCommandParse(String string, ParseResults<CommandSourceStack> parse);
	}
}
