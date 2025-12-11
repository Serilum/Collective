package com.natamus.collective.neoforge.mixin;

import com.google.common.hash.HashCode;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ClientPacketListener.class, priority = 1001)
public class ClientPacketListenerMixin {
	@Inject(method = "lambda$new$2", at = @At(value = "HEAD"), cancellable = true)
    private static void decoratedHashOpsGenerator(RegistryOps<?> registryOps, TypedDataComponent<?> typedDataComponent, CallbackInfoReturnable<Integer> cir) {
		try {
            HashCode hashCode = (HashCode) typedDataComponent.encodeValue(registryOps).getOrThrow((string) -> {
				return new IllegalArgumentException();
            });
		}
		catch (IllegalArgumentException ex) {
			cir.setReturnValue(-1);
		}
	}
}