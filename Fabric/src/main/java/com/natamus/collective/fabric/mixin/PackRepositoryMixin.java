package com.natamus.collective.fabric.mixin;

import com.natamus.collective.translations.TranslationPack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = PackRepository.class, priority = 1001)
public class PackRepositoryMixin {
	@Shadow @Final @Mutable private Set<RepositorySource> sources;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void collective$addTranslationPack(RepositorySource[] sources, CallbackInfo ci) {
		Set<RepositorySource> updated = new HashSet<>(this.sources);
		updated.add(TranslationPack::contribute);
		this.sources = updated;
	}
}
