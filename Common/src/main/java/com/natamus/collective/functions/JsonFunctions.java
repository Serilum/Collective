package com.natamus.collective.functions;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonFunctions {
	public static HashMap<String, String> JsonStringToHashMap(String jsonstring) {
		return new Gson().fromJson(jsonstring, new TypeToken<HashMap<String, String>>(){}.getType());
	}
	
	public static String HashMapToJsonString(HashMap<String, String> map) {
		return new Gson().toJson(map); 
	}
}
