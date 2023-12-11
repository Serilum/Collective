package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.natamus.collective.fabric.callbacks.CollectiveCommandEvents;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

@Mixin(value = Commands.class, priority = 1001)
public class CommandsMixin {
	@Shadow private @Final CommandDispatcher<CommandSourceStack> dispatcher;
	
    @Inject(method = "performCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)I", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)I"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void Commands_performCommand(CommandSourceStack commandSourceStack, String string, CallbackInfoReturnable<Integer> cir, StringReader stringReader) {
    	ParseResults<CommandSourceStack> parse = this.dispatcher.parse(stringReader, commandSourceStack);
    	CollectiveCommandEvents.ON_COMMAND_PARSE.invoker().onCommandParse(string, parse);
    }
}
