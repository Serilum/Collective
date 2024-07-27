package com.natamus.collective.neoforge.bundle;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;

import java.io.File;

public class NeoForgeBundleJarJarCheck {
	@SuppressWarnings("UnstableApiUsage")
	public static boolean isModJarJard(String modId) {
		boolean isJarJar = false;

		LoadingModList modListInstance = LoadingModList.get();
		if (modListInstance != null) {
			ModFileInfo modFileInfo = modListInstance.getModFileById(modId);

			if (modFileInfo != null) {
				ModFile modFile = modFileInfo.getFile();
				ModFileDiscoveryAttributes modFileDiscoveryAttributes = modFile.getDiscoveryAttributes();
				if (modFileDiscoveryAttributes != null) {
					IModFile parentModFile = modFile.getDiscoveryAttributes().parent();

					isJarJar = parentModFile != null;
				}
				else {
					String location = modFile.getFilePath().toString();

					isJarJar = !location.contains("mods" + File.separator);
				}
			}
		}

		return isJarJar;
	}
}
