package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveEntityEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = TeleportCommand.class, priority = 1001)
public abstract class TeleportCommandMixin {
	@Inject(method = "performTeleport(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFLnet/minecraft/server/commands/TeleportCommand$LookAt;)V", at = @At(value = "HEAD"), cancellable = true)
	private static void performTeleport(CommandSourceStack commandSourceStack, Entity entity, ServerLevel serverLevel, double d, double e, double f, Set<RelativeMovement> set, float g, float h, TeleportCommand.LookAt lookAt, CallbackInfo ci) {
		if (!CollectiveEntityEvents.ON_ENTITY_TELEPORT_COMMAND.invoker().onTeleportCommand(serverLevel, entity, d, e, f)) {
			ci.cancel();
		}
	}
}
