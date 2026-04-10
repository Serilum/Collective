package com.natamus.collective.forge.bundle;

import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;

import java.io.File;

public class ForgeBundleJarJarCheck {
	public static boolean isModJarJard(String modId) {
		boolean isJarJar = false;

		ModFileInfo modFileInfo = LoadingModList.getModFileById(modId);
		if (modFileInfo != null) {
			ModFile modFile = modFileInfo.getFile();
			String location = modFile.getFilePath().toString();

			isJarJar = !location.contains("mods" + File.separator);
		}

		return isJarJar;
	}
}
