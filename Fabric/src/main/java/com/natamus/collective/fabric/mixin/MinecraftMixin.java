package com.natamus.collective.fabric.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.natamus.collective.data.Constants;
import com.natamus.collective.fabric.callbacks.CollectiveLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 1001)
public class MinecraftMixin {
    @Inject(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V", at = @At(value = "TAIL"))
    public void Minecraft_init(GameConfig gameConfig, CallbackInfo ci) {
        ((Minecraft)(Object)this).execute(() -> {
            CollectiveLifecycleEvents.MINECRAFT_LOADED.invoker().onMinecraftLoad(true);
        });
    }

    @Inject(method = "createUserApiService", at = @At(value = "HEAD"), cancellable = true)
    public void Minecraft_createUserApiService(YggdrasilAuthenticationService yggdrasilAuthenticationService, GameConfig gameConfig, CallbackInfoReturnable<UserApiService> cir) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Constants.LOG.info("Failed to verify authentication");
            cir.setReturnValue(UserApiService.OFFLINE);
        }
    }
}
