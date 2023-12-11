package com.natamus.collective.functions;

import com.natamus.collective.services.Services;
import net.minecraft.SharedConstants;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataFunctions {
	public static String readStringFromURL(String requestURL) {
		String data = "";
	    try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8)) {
	        scanner.useDelimiter("\\A");
	        data = scanner.hasNext() ? scanner.next() : "";
	    }
	    catch(Exception ignored) {}
	    
	    return data;
	}

	@SuppressWarnings("deprecation")
	public static String getCurrentMinecraftVersion() {
		return SharedConstants.VERSION_STRING;
	}

	public static String getGameDirectory() {
		return Services.MODLOADER.getGameDirectory();
	}
	public static Path getGameDirectoryPath() {
		return Path.of(getGameDirectory());
	}
	public static String getModDirectory() {
		return getGameDirectory() + File.separator + "mods";
	}
	public static Path getModDirectoryPath() {
		return Path.of(getModDirectory());
	}
	public static String getConfigDirectory() {
		return getGameDirectory() + File.separator + "config";
	}
	public static Path getConfigDirectoryPath() {
		return Path.of(getConfigDirectory());
	}


	
	public static List<String> getInstalledModJars() {
		List<String> installedmods = new ArrayList<String>();
		
		File mainFolder = new File(getModDirectory());
		File[] listOfMainFiles = mainFolder.listFiles();
		File subFolder = new File(getModDirectory() + File.separator + getCurrentMinecraftVersion());
		File[] listOfSubFiles = subFolder.listFiles();

		for (File file : ArrayUtils.addAll(listOfMainFiles, listOfSubFiles)) {
		    if (file.isFile()) {
		        String filename = file.getName().replaceAll(" +\\([0-9]+\\)", "");
		        installedmods.add(filename);
		    }
		}

		return installedmods;
	}
	
	public static byte setBit(byte b, int i, boolean bo) {
		if (bo) {
			b = (byte)(b | i);
		}
		else {
			b = (byte)(b & ~i);
		}
		
		return b;
	}
}
