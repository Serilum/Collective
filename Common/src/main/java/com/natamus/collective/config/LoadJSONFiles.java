package com.natamus.collective.config;

import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.functions.JsonFunctions;
import com.natamus.collective.globalcallbacks.JSONCallback;

public class LoadJSONFiles {
	public static void startListening() {
		JSONCallback.ALL_JSON_FILES_AVAILABLE.register((String folder) -> {
			GlobalVariables.areaNames = JsonFunctions.getStringListFromJsonFile(folder, "area_names.json", "area_names");
			GlobalVariables.femaleNames = JsonFunctions.getStringListFromJsonFile(folder, "entity_names.json", "female_names");
			GlobalVariables.maleNames = JsonFunctions.getStringListFromJsonFile(folder, "entity_names.json", "male_names");
			GlobalVariables.lingerMessages = JsonFunctions.getStringListFromJsonFile(folder, "linger_messages.json", "linger_messages");
		});
	}
}
