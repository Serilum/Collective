package com.natamus.collective.fabric.mixin;

import com.mojang.datafixers.DataFixer;
import com.natamus.collective.fabric.callbacks.CollectiveLifecycleEvents;
import com.natamus.collective.fabric.callbacks.CollectiveMinecraftServerEvents;
import com.natamus.collective.fabric.callbacks.CollectiveWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;

@Mixin(value = MinecraftServer.class, priority = 1001)
public class MinecraftServerMixin {
	@Inject(method = "<init>(Ljava/lang/Thread;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/server/WorldStem;Ljava/net/Proxy;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/server/Services;Lnet/minecraft/server/level/progress/ChunkProgressListenerFactory;)V", at = @At(value = "TAIL"))
	public void MinecraftServer_init(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory, CallbackInfo ci) {
		((MinecraftServer)(Object)this).execute(() -> {
			CollectiveLifecycleEvents.MINECRAFT_LOADED.invoker().onMinecraftLoad(false);
		});
	}

	@Inject(method = "setInitialSpawn", at = @At(value = "RETURN"))
	private static void MinecraftServer_setInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean bl, boolean bl2, CallbackInfo ci) {
		CollectiveMinecraftServerEvents.WORLD_SET_SPAWN.invoker().onSetSpawn(serverLevel, serverLevelData);
	}
	
	@ModifyVariable(method = "stopServer", at = @At(value= "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;close()V", ordinal = 0))
	public ServerLevel MinecraftServer_stopServer(ServerLevel serverlevel1) {
		CollectiveWorldEvents.WORLD_UNLOAD.invoker().onWorldUnload(serverlevel1);
		return serverlevel1;
	}
}
