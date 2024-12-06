package com.natamus.collective.functions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigFunctions {
	public static List<String> getRawConfigValues(String modid) {
		return getRawConfigValues(modid, false);
	}
	
	public static List<String> getRawConfigValues(String modid, boolean tve) {
		String dirpath = DataFunctions.getConfigDirectory();
		if (tve) {
			dirpath = dirpath + File.separator + "TVE";
		}
		
		File dir = new File(dirpath);
		File file = new File(dirpath + File.separator + modid + ".json5");
		
		List<String> values = new ArrayList<String>();
		if (dir.isDirectory() && file.isFile()) {
			try {
				String content = new String(Files.readAllBytes(Paths.get(dirpath + File.separator + modid + ".json5")));
				for (String line : content.split("\n")) {
					String trimmedline = line.trim();
					if (!trimmedline.startsWith("\"")) {
						continue;
					}
					
					if (trimmedline.endsWith(",")) {
						trimmedline = trimmedline.substring(0, trimmedline.length()-1);
					}
					
					values.add(trimmedline);
				}
				
			} catch (IOException ignored) { }
		}
		
		return values;
	}

	public static HashMap<String, String> getDictValues(String modid) {
		return getDictValues(modid, false);
	}

	public static HashMap<String, String> getDictValues(String modid, boolean tve) {
		HashMap<String, String> hm = new HashMap<String, String>();

		List<String> rawvalues = getRawConfigValues(modid, tve);
		for (String rawvalue : rawvalues) {
			if (rawvalue.length() < 3) {
				continue;
			}

			String rv = rawvalue.replace("\"", "");

			String[] rvspl;
			if (rv.contains("=")) {
				rvspl = rv.split("=");
			}
			else if (rv.contains(":")) {
				rvspl = rv.split(":");
			}
			else {
				continue;
			}

			if (rvspl.length < 2) {
				continue;
			}

			String keystr = rvspl[0].trim();
			String valuestr = rvspl[1].trim();

			hm.put(keystr, valuestr);
		}

		return hm;
	}
}
