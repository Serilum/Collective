package com.natamus.collective.fakeplayer;

import com.mojang.authlib.GameProfile;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

//Preliminary, simple Fake Player class
public class FakePlayer extends ServerPlayer {
	    public FakePlayer(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile,
			ServerPlayerGameMode serverPlayerGameMode) {
		super(minecraftServer, serverLevel, gameProfile, null);
	}
		@Override public @NotNull Vec3 position(){ return new Vec3(0, 0, 0); }
	    @Override public @NotNull BlockPos blockPosition(){ return BlockPos.ZERO; }
	    @Override public void displayClientMessage(@NotNull Component chatComponent, boolean actionBar){}
	    public void sendMessage(@NotNull Component component, @NotNull UUID senderUUID) {}
	    @Override public void awardStat(@NotNull Stat<?> par1StatBase, int par2){}
	    //@Override public void openGui(Object mod, int modGuiId, World world, int x, int y, int z){}
	    @Override public boolean isInvulnerableTo(@NotNull DamageSource source){ return true; }
	    @Override public boolean canHarmPlayer(@NotNull Player player){ return false; }
	    @Override public void die(@NotNull DamageSource source){ }
	    @Override public void tick(){ }
	    @Override public void updateOptions(@NotNull ServerboundClientInformationPacket pkt){ }

	    @SuppressWarnings("unused")
		private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
	        private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.CLIENTBOUND);

	        public FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) {
	            super(server, DUMMY_CONNECTION, player);
	        }

	        @Override public void tick() { }
	        @Override public void resetPosition() { }
	        @Override public void disconnect(@NotNull Component message) { }
	        @Override public void handlePlayerInput(@NotNull ServerboundPlayerInputPacket packet) { }
	        @Override public void handleMoveVehicle(@NotNull ServerboundMoveVehiclePacket packet) { }
	        @Override public void handleAcceptTeleportPacket(@NotNull ServerboundAcceptTeleportationPacket packet) { }
	        @Override public void handleRecipeBookSeenRecipePacket(@NotNull ServerboundRecipeBookSeenRecipePacket packet) { }
	        @Override public void handleRecipeBookChangeSettingsPacket(@NotNull ServerboundRecipeBookChangeSettingsPacket packet) { }
	        @Override public void handleSeenAdvancements(@NotNull ServerboundSeenAdvancementsPacket packet) { }
	        @Override public void handleCustomCommandSuggestions(@NotNull ServerboundCommandSuggestionPacket packet) { }
	        @Override public void handleSetCommandBlock(@NotNull ServerboundSetCommandBlockPacket packet) { }
	        @Override public void handleSetCommandMinecart(@NotNull ServerboundSetCommandMinecartPacket packet) { }
	        @Override public void handlePickItem(@NotNull ServerboundPickItemPacket packet) { }
	        @Override public void handleRenameItem(@NotNull ServerboundRenameItemPacket packet) { }
	        @Override public void handleSetBeaconPacket(@NotNull ServerboundSetBeaconPacket packet) { }
	        @Override public void handleSetStructureBlock(@NotNull ServerboundSetStructureBlockPacket packet) { }
	        @Override public void handleSetJigsawBlock(@NotNull ServerboundSetJigsawBlockPacket packet) { }
	        @Override public void handleJigsawGenerate(@NotNull ServerboundJigsawGeneratePacket packet) { }
	        @Override public void handleSelectTrade(@NotNull ServerboundSelectTradePacket packet) { }
	        @Override public void handleEditBook(@NotNull ServerboundEditBookPacket packet) { }
	        @Override public void handleEntityTagQuery(@NotNull ServerboundEntityTagQuery packet) { }
	        @Override public void handleBlockEntityTagQuery(@NotNull ServerboundBlockEntityTagQuery packet) { }
	        @Override public void handleMovePlayer(@NotNull ServerboundMovePlayerPacket packet) { }
	        @Override public void teleport(double x, double y, double z, float yaw, float pitch) { }
	        @Override public void teleport(double x, double y, double z, float yaw, float pitch, @NotNull Set<ClientboundPlayerPositionPacket.RelativeArgument> flags) { }
	        @Override public void handlePlayerAction(@NotNull ServerboundPlayerActionPacket packet) { }
	        @Override public void handleUseItemOn(@NotNull ServerboundUseItemOnPacket packet) { }
	        @Override public void handleUseItem(@NotNull ServerboundUseItemPacket packet) { }
	        @Override public void handleTeleportToEntityPacket(@NotNull ServerboundTeleportToEntityPacket packet) { }
	        @Override public void handleResourcePackResponse(@NotNull ServerboundResourcePackPacket packet) { }
	        @Override public void handlePaddleBoat(@NotNull ServerboundPaddleBoatPacket packet) { }
	        @Override public void onDisconnect(@NotNull Component message) { }
	        @Override public void send(@NotNull Packet<?> packet) { }
	        public void send(@NotNull Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener) { }
	        @Override public void handleSetCarriedItem(@NotNull ServerboundSetCarriedItemPacket packet) { }
	        @Override public void handleChat(@NotNull ServerboundChatPacket packet) { }
	        @Override public void handleAnimate(@NotNull ServerboundSwingPacket packet) { }
	        @Override public void handlePlayerCommand(@NotNull ServerboundPlayerCommandPacket packet) { }
	        @Override public void handleInteract(@NotNull ServerboundInteractPacket packet) { }
	        @Override public void handleClientCommand(@NotNull ServerboundClientCommandPacket packet) { }
	        @Override public void handleContainerClose(@NotNull ServerboundContainerClosePacket packet) { }
	        @Override public void handleContainerClick(@NotNull ServerboundContainerClickPacket packet) { }
	        @Override public void handlePlaceRecipe(@NotNull ServerboundPlaceRecipePacket packet) { }
	        @Override public void handleContainerButtonClick(@NotNull ServerboundContainerButtonClickPacket packet) { }
	        @Override public void handleSetCreativeModeSlot(@NotNull ServerboundSetCreativeModeSlotPacket packet) { }
	        @Override public void handleSignUpdate(@NotNull ServerboundSignUpdatePacket packet) { }
	        @Override public void handleKeepAlive(@NotNull ServerboundKeepAlivePacket packet) { }
	        @Override public void handlePlayerAbilities(@NotNull ServerboundPlayerAbilitiesPacket packet) { }
	        @Override public void handleClientInformation(@NotNull ServerboundClientInformationPacket packet) { }
	        @Override public void handleCustomPayload(@NotNull ServerboundCustomPayloadPacket packet) { }
	        @Override public void handleChangeDifficulty(@NotNull ServerboundChangeDifficultyPacket packet) { }
	        @Override public void handleLockDifficulty(@NotNull ServerboundLockDifficultyPacket packet) { }
	    }
}