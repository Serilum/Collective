package com.natamus.collective.config.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.natamus.collective.services.Services;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

public class DuskConfigModIcons {
	private static final HashMap<String, DuskConfigModIcons> icons = new HashMap<>();

	public final Identifier texture;
	public final int width;
	public final int height;

	private DuskConfigModIcons(Identifier texture, int width, int height) {
		this.texture = texture;
		this.width = width;
		this.height = height;
	}

	public static DuskConfigModIcons getIcon(String modid) {
		if (icons.containsKey(modid)) {
			return icons.get(modid);
		}

		DuskConfigModIcons icon = loadIcon(modid);
		icons.put(modid, icon);
		return icon;
	}

	private static DuskConfigModIcons loadIcon(String modid) {
		byte[] iconBytes = Services.MODLOADER.getModIcon(modid);
		if (iconBytes == null) {
			return null;
		}

		NativeImage image;
		try {
			image = NativeImage.read(new ByteArrayInputStream(iconBytes));
		}
		catch (IOException ignored) {
			return null;
		}

		Identifier texture = Identifier.fromNamespaceAndPath(CollectiveReference.MOD_ID, "modicon/" + modid);
		DuskConfigModIcons icon = new DuskConfigModIcons(texture, image.getWidth(), image.getHeight());
		Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(() -> "Collective mod icon " + modid, image));
		return icon;
	}
}
