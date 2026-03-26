package com.natamus.collective.fabric.mixin;

import com.mojang.serialization.Lifecycle;
import com.natamus.collective.services.Services;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(value = CreateWorldScreen.class, priority = 1001)
public abstract class CreateWorldScreenMixin {
    @Shadow
    private boolean recreated;

    @Shadow
    protected abstract boolean createNewWorld(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, LevelDataAndDimensions.WorldDataAndGenSettings worldDataAndGenSettings, Optional<GameRules> gameRules);

    @Inject(method = "onCreate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;confirmWorldCreation(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onCreate(CallbackInfo ci, WorldCreationContext worldCreationContext, WorldDimensions worldDimensions, WorldDimensions.Complete complete, LayeredRegistryAccess layeredRegistryAccess, FeatureFlagSet enabledFeatures, Lifecycle lifecycleFromFeatures, Lifecycle lifecycleFromRegistries, Lifecycle lifecycle, boolean bl, boolean isDebug, LevelSettings levelSettings, GameRules gameRules, PrimaryLevelData primaryLevelData, WorldOptions options, WorldGenSettings worldGenSettings, LevelDataAndDimensions.WorldDataAndGenSettings worldDataAndGenSettings) {
        if (this.recreated) {
            return;
        }

        if (Services.MODLOADER.isDevelopmentEnvironment() || Services.MODLOADER.isModLoaded("hideexperimentalwarning")) {
            this.createNewWorld(layeredRegistryAccess, worldDataAndGenSettings, Optional.of(gameRules));
            ci.cancel();
        }
    }
}