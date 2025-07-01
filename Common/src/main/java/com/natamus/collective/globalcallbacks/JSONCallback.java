package com.natamus.collective.globalcallbacks;

import com.google.gson.JsonElement;
import com.natamus.collective.implementations.event.Event;
import com.natamus.collective.implementations.event.EventFactory;

import javax.annotation.Nullable;

public class JSONCallback {
	private JSONCallback() { }

    public static final Event<On_Json_File_Available> JSON_FILE_AVAILABLE = EventFactory.createArrayBacked(On_Json_File_Available.class, callbacks -> (folder, fileName, isCreated, jsonElement) -> {
        for (On_Json_File_Available callback : callbacks) {
        	callback.onJsonFileAvailable(folder, fileName, isCreated, jsonElement);
        }
    });

    public static final Event<All_Json_Files_Available> ALL_JSON_FILES_AVAILABLE = EventFactory.createArrayBacked(All_Json_Files_Available.class, callbacks -> (folder) -> {
        for (All_Json_Files_Available callback : callbacks) {
        	callback.onAllJsonFilesAvailable(folder);
        }
    });

	@FunctionalInterface
	public interface On_Json_File_Available {
		 void onJsonFileAvailable(String folder, String fileName, boolean isCreated, @Nullable JsonElement jsonElement);
	}

	@FunctionalInterface
	public interface All_Json_Files_Available {
		 void onAllJsonFilesAvailable(String folder);
	}
}
