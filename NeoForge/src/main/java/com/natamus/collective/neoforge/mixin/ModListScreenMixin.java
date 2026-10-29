package com.natamus.collective.neoforge.mixin;

import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforgespi.language.IModInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(value = ModListScreen.class, priority = 1001)
public class ModListScreenMixin {
	// Temporary fix for iconFile mismatch in some dependent 26.1.2 mod versions
	@Redirect(method = "updateCache()V", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforgespi/language/IModInfo;getLogoFile()Ljava/util/Optional;"))
	private Optional<String> updateCache_getLogoFile(IModInfo modInfo) {
		Optional<String> logoFile = modInfo.getLogoFile();
		if (logoFile.isPresent()) {
			return logoFile;
		}

		Optional<String> authors = modInfo.getConfig().getConfigElement("authors");
		if (!authors.isPresent() || !authors.get().equals("Serilum")) {
			return logoFile;
		}

		Optional<String> modIconFile = modInfo.getConfig().getConfigElement("iconFile");
		if (modIconFile.isPresent()) {
			return modIconFile;
		}

		return modInfo.getOwningFile().getConfig().getConfigElement("iconFile");
	}
}
