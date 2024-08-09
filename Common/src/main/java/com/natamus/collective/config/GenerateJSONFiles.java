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

public class GenerateJSONFiles {
	private static final List<String> requestedJsonFiles = new ArrayList<String>();

	public static void requestJSONFile(String modid, List<String> fileNames) {
		for (String fileName : fileNames) {
			requestJSONFile(modid, fileName);
		}
	}
	public static void requestJSONFile(String modid, String fileName) {
		if (!requestedJsonFiles.contains(fileName)) {
			requestedJsonFiles.add(fileName);
		}

		Constants.LOG.info("[" + CollectiveReference.NAME + "] JSON file '" + fileName + "' generation requested by mod '" + modid + "'.");
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

			File file = new File(dirpath + File.separator + fileName);
			if (!file.isFile()) {
				InputStream inputStream = DataFunctions.getDataInputStream(minecraftServer, CollectiveReference.MOD_ID, "json", fileName);
				if (inputStream != null) {
					jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

					PrintWriter writer = null;
					try {
						writer = new PrintWriter(dirpath + File.separator + fileName, StandardCharsets.UTF_8);
						writer.print(Constants.GSON.toJson(jsonElement));
					} catch (IOException ex) {
						Constants.LOG.warn("[" + CollectiveReference.NAME + "] Unable to write the JSON file: " + fileName);
					}

					if (writer != null) {
						writer.close();
					}
				}
				else {
					Constants.LOG.warn("[" + CollectiveReference.NAME + "] Unable to get Input Stream for: " + fileName);
				}
			}

			JSONCallback.JSON_FILE_AVAILABLE.invoker().onJsonFileAvailable(dirpath, fileName, isCreated, jsonElement);
		}

		JSONCallback.ALL_JSON_FILES_AVAILABLE.invoker().onAllJsonFilesAvailable(dirpath);
	}
}
