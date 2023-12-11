package com.natamus.collective.functions;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.natamus.collective.data.Constants;
import com.natamus.collective.util.CollectiveReference;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonFunctions {
	public static HashMap<String, String> JsonStringToHashMap(String jsonstring) {
		return new Gson().fromJson(jsonstring, new TypeToken<HashMap<String, String>>(){}.getType());
	}
	
	public static String HashMapToJsonString(HashMap<String, String> map) {
		return new Gson().toJson(map); 
	}

	public static List<String> getStringListFromJsonFile(String folder, String fileName, String key) {
		folder = folder.replace("/", File.separator).replace("\\", File.separator).strip();
		if (!folder.endsWith(File.separator)) {
			folder = folder + File.separator;
		}

		List<String> stringList = new ArrayList<String>();

		File file = new File(folder + fileName);
		if (!file.isFile()) {
			return stringList;
		}

		JsonElement jsonElement = null;
		try {
			jsonElement = getJsonElementFromFile(folder, fileName);
		}
		catch (IOException ex) {
			Constants.LOG.warn("[" + CollectiveReference.NAME + "] IOException while trying to parse JSON file: " + folder + fileName);
			return stringList;
		}

		if (jsonElement == null) {
			return stringList;
		}

		if (!jsonElement.isJsonObject()) {
			return stringList;
		}

		JsonObject jsonObject = jsonElement.getAsJsonObject();

		JsonArray jsonArray = jsonObject.getAsJsonArray(key);
		if (jsonArray == null) {
			Constants.LOG.warn("[" + CollectiveReference.NAME + "] Unable to find JSON member with key: " + key);
			return stringList;
		}

		for (JsonElement jE : jsonArrayToList(jsonArray)) {
			try {
				String stringElement = jE.getAsString();
				stringList.add(stringElement);
			}
			catch (Exception ex) {
				Constants.LOG.warn("[" + CollectiveReference.NAME + "] Unable to parse '" + jE.toString() + "' as a String Element.");
			}
		}

		return stringList;
	}
	public static JsonElement getJsonElementFromFile(String folder, String fileName) throws IOException {
		return JsonParser.parseReader(new FileReader(folder + fileName));
	}

	public static List<JsonElement> jsonArrayToList(JsonArray jsonArray) {
		List<JsonElement> jsonElementList = new ArrayList<JsonElement>();
		if (jsonArray != null) {
			for (int i=0; i < jsonArray.size(); i++){
				jsonElementList.add(jsonArray.get(i));
			}
		}
		return jsonElementList;
	}
}
