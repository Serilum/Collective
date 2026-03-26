package com.natamus.collective.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FakePlayer extends ServerPlayer {
	MinecraftServer minecraftServer;

    public FakePlayer(ServerLevel level, GameProfile name) {
        super(level.getServer(), level, name, ClientInformation.createDefault());
		minecraftServer = level.getServer();
        //this.connection = new FakePlayerNetHandler(level.getServer(), this);
    }

    @Override
    public void awardStat(@NotNull Stat stat, int amount) {}

    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return true;
    }

    @Override
    public boolean canHarmPlayer(@NotNull Player player) {
        return false;
    }

    @Override
    public void die(@NotNull DamageSource source) {}

    @Override
    public void tick() {}

    @Override
    public void updateOptions(@NotNull ClientInformation p_301998_) {}

    @Nullable
    public MinecraftServer getServer() {
        return minecraftServer;
    }

    private static class FakePlayerNetHandler extends ServerGamePacketListenerImpl {
        private static final net.minecraft.network.Connection DUMMY_CONNECTION = new net.minecraft.network.Connection(PacketFlow.CLIENTBOUND);

        public FakePlayerNetHandler(MinecraftServer server, ServerPlayer player) {
            super(server, DUMMY_CONNECTION, player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
        }

        @Override
        public void tick() {}

        @Override
        public void resetPosition() {}

        @Override
        public void disconnect(@NotNull Component message) {}

        @Override
        public void handlePlayerInput(@NotNull ServerboundPlayerInputPacket packet) {}

        @Override
        public void handleMoveVehicle(@NotNull ServerboundMoveVehiclePacket packet) {}

        @Override
        public void handleAcceptTeleportPacket(@NotNull ServerboundAcceptTeleportationPacket packet) {}

        @Override
        public void handleRecipeBookSeenRecipePacket(@NotNull ServerboundRecipeBookSeenRecipePacket packet) {}

        @Override
        public void handleRecipeBookChangeSettingsPacket(@NotNull ServerboundRecipeBookChangeSettingsPacket packet) {}

        @Override
        public void handleSeenAdvancements(@NotNull ServerboundSeenAdvancementsPacket packet) {}

        @Override
        public void handleCustomCommandSuggestions(@NotNull ServerboundCommandSuggestionPacket packet) {}

        @Override
        public void handleSetCommandBlock(@NotNull ServerboundSetCommandBlockPacket packet) {}

        @Override
        public void handleSetCommandMinecart(@NotNull ServerboundSetCommandMinecartPacket packet) {}

        @Override
        public void handleRenameItem(@NotNull ServerboundRenameItemPacket packet) {}

        @Override
        public void handleSetBeaconPacket(@NotNull ServerboundSetBeaconPacket packet) {}

        @Override
        public void handleSetStructureBlock(@NotNull ServerboundSetStructureBlockPacket packet) {}

        @Override
        public void handleSetJigsawBlock(@NotNull ServerboundSetJigsawBlockPacket packet) {}

        @Override
        public void handleJigsawGenerate(@NotNull ServerboundJigsawGeneratePacket packet) {}

        @Override
        public void handleSelectTrade(@NotNull ServerboundSelectTradePacket packet) {}

        @Override
        public void handleEditBook(@NotNull ServerboundEditBookPacket packet) {}

        @Override
        public void handleMovePlayer(@NotNull ServerboundMovePlayerPacket packet) {}

        @Override
        public void teleport(double x, double y, double z, float yaw, float pitch) {}

        @Override
        public void handlePlayerAction(@NotNull ServerboundPlayerActionPacket packet) {}

        @Override
        public void handleUseItemOn(@NotNull ServerboundUseItemOnPacket packet) {}

        @Override
        public void handleUseItem(@NotNull ServerboundUseItemPacket packet) {}

        @Override
        public void handleTeleportToEntityPacket(@NotNull ServerboundTeleportToEntityPacket packet) {}

        @Override
        public void handleResourcePackResponse(@NotNull ServerboundResourcePackPacket p_295695_) {}

        @Override
        public void handlePaddleBoat(@NotNull ServerboundPaddleBoatPacket packet) {}

        @Override
        public void send(@NotNull Packet<?> packet) {}

        @Override
        public void handleSetCarriedItem(@NotNull ServerboundSetCarriedItemPacket packet) {}

        @Override
        public void handleChat(@NotNull ServerboundChatPacket packet) {}

        @Override
        public void handleAnimate(@NotNull ServerboundSwingPacket packet) {}

        @Override
        public void handlePlayerCommand(@NotNull ServerboundPlayerCommandPacket packet) {}

        @Override
        public void handleInteract(@NotNull ServerboundInteractPacket packet) {}

        @Override
        public void handleClientCommand(@NotNull ServerboundClientCommandPacket packet) {}

        @Override
        public void handleContainerClose(@NotNull ServerboundContainerClosePacket packet) {}

        @Override
        public void handleContainerClick(@NotNull ServerboundContainerClickPacket packet) {}

        @Override
        public void handlePlaceRecipe(@NotNull ServerboundPlaceRecipePacket packet) {}

        @Override
        public void handleContainerButtonClick(@NotNull ServerboundContainerButtonClickPacket packet) {}

        @Override
        public void handleSetCreativeModeSlot(@NotNull ServerboundSetCreativeModeSlotPacket packet) {}

        @Override
        public void handleSignUpdate(@NotNull ServerboundSignUpdatePacket packet) {}

        @Override
        public void handleKeepAlive(@NotNull ServerboundKeepAlivePacket p_294627_) {}

        @Override
        public void handleCustomPayload(@NotNull ServerboundCustomPayloadPacket p_294276_) {}

        @Override
        public void handleClientInformation(@NotNull ServerboundClientInformationPacket p_301979_) {}

        @Override
        public void handlePlayerAbilities(@NotNull ServerboundPlayerAbilitiesPacket packet) {}

        @Override
        public void handleChangeDifficulty(@NotNull ServerboundChangeDifficultyPacket packet) {}

        @Override
        public void handleLockDifficulty(@NotNull ServerboundLockDifficultyPacket packet) {}

        public void teleport(double x, double y, double z, float yaw, float pitch, Set<?> relativeSet) {}

        @Override
        public void ackBlockChangesUpTo(int sequence) {}

        @Override
        public void handleChatCommand(@NotNull ServerboundChatCommandPacket packet) {}

        @Override
        public void handleChatAck(@NotNull ServerboundChatAckPacket packet) {}

        @Override
        public void sendPlayerChatMessage(@NotNull PlayerChatMessage message, ChatType.@NotNull Bound boundChatType) {}

        @Override
        public void sendDisguisedChatMessage(@NotNull Component content, ChatType.@NotNull Bound boundChatType) {}

        @Override
        public void handleChatSessionUpdate(@NotNull ServerboundChatSessionUpdatePacket packet) {}
    }
}