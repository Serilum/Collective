package com.natamus.collective.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.natamus.collective.data.Constants;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.collective.globalcallbacks.JSONCallback;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GenerateJSONFiles {
	private static final List<String> requestedJsonFiles = new CopyOnWriteArrayList<>();
	private static final Map<String, List<String>> requestedJsonFilesPerMod = new ConcurrentHashMap<>();

	public static void requestJSONFile(String modid, List<String> fileNames) {
		for (String fileName : fileNames) {
			requestJSONFile(modid, fileName);
		}
	}
	public static synchronized void requestJSONFile(String modid, String fileName) {
		if (!requestedJsonFiles.contains(fileName)) {
			requestedJsonFiles.add(fileName);
		}

		List<String> modFiles = requestedJsonFilesPerMod.get(modid);
		if (modFiles == null) {
			modFiles = new CopyOnWriteArrayList<>();
			requestedJsonFilesPerMod.put(modid, modFiles);
		}
		if (!modFiles.contains(fileName)) {
			modFiles.add(fileName);
		}

		createFromBundledFileIfMissing(fileName);

		Constants.LOG.info("JSON file '" + fileName + "' generation requested by mod '" + modid + "'.");
	}

	private static void createFromBundledFileIfMissing(String fileName) {
		File file = getJsonFile(fileName);
		if (file.isFile()) {
			return;
		}

		try (InputStream inputStream = GenerateJSONFiles.class.getResourceAsStream("/data/" + CollectiveReference.MOD_ID + "/json/" + fileName)) {
			if (inputStream != null) {
				writeJsonFile(file, JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
			}
		}
		catch (IOException ignored) { }
	}

	static File getJsonFile(String fileName) {
		return new File(DataFunctions.getConfigDirectory() + File.separator + CollectiveReference.MOD_ID + File.separator + fileName);
	}

	static void writeJsonFile(File file, JsonElement jsonElement) {
		boolean ignored = file.getParentFile().mkdirs();

		PrintWriter writer;
		try {
			writer = new PrintWriter(file, StandardCharsets.UTF_8);
		}
		catch (IOException ex) {
			Constants.LOG.warn("Unable to write the JSON file: " + file.getName());
			return;
		}

		writer.print(Constants.GSON.toJson(jsonElement));
		writer.close();
	}

	public static List<String> getRequestedFiles(String modid) {
		List<String> modFiles = requestedJsonFilesPerMod.get(modid);
		if (modFiles == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(modFiles);
	}

	public static void initGeneration(ServerLevel serverLevel) {
		initGeneration(serverLevel.getServer());
	}
	public static void initGeneration(MinecraftServer minecraftServer) {
		if (requestedJsonFiles.isEmpty()) {
			return;
		}

		String dirpath = DataFunctions.getConfigDirectory() + File.separator + CollectiveReference.MOD_ID;
		File dir = new File(dirpath);
		if (!dir.isDirectory()) {
			boolean ignored = dir.mkdirs();
		}

		for (String fileName : requestedJsonFiles) {
			boolean isCreated = false;
			JsonElement jsonElement = null;

			File file = getJsonFile(fileName);
			if (!file.isFile()) {
				InputStream inputStream = DataFunctions.getDataInputStream(minecraftServer, CollectiveReference.MOD_ID, "json", fileName);
				if (inputStream != null) {
					jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
					writeJsonFile(file, jsonElement);
				}
				else {
					Constants.LOG.warn("Unable to get Input Stream for: " + fileName);
				}
			}

			JSONCallback.JSON_FILE_AVAILABLE.invoker().onJsonFileAvailable(dirpath, fileName, isCreated, jsonElement);
		}

		JSONCallback.ALL_JSON_FILES_AVAILABLE.invoker().onAllJsonFilesAvailable(dirpath);
	}
}
