package com.natamus.collective.forge.mixin;

import com.natamus.collective.config.DuskConfigModToggles;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = FileToIdConverter.class, priority = 1001)
public class FileToIdConverterMixin {
	@Shadow @Final private String prefix;

	@Inject(method = "listMatchingResources(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;", at = @At(value = "RETURN"))
	private void listMatchingResources(ResourceManager manager, CallbackInfoReturnable<Map<Identifier, Resource>> cir) {
		if (!prefix.equals(Registries.elementsDirPath(Registries.RECIPE))) {
			return;
		}

		cir.getReturnValue().keySet().removeIf(recipeFile -> DuskConfigModToggles.isDisabledSerilumMod(recipeFile.getNamespace()));
	}
}
