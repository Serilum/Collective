package com.natamus.collective.forge.mixin;

import com.natamus.collective.data.BlockEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerLevel.class, priority = 1001)
public class ServerLevelMixin {
	@Inject(method = "close()V", at = @At(value = "TAIL"))
	public void serverLevel_close(CallbackInfo ci) {
		BlockEntityData.removeLevelFromCache((Level)(Object)this);
	}
}
