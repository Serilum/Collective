package com.natamus.collective.networking.packets;

import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.services.Services;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntityDataSyncPacket {
    public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(CollectiveReference.MOD_ID, "entity_data_sync");

    public final int entityId;
    public final CompoundTag data;

    public EntityDataSyncPacket(int entityId, CompoundTag data) {
        this.entityId = entityId;
        this.data = data;
    }

    public static EntityDataSyncPacket decode(FriendlyByteBuf buf) {
        return new EntityDataSyncPacket(buf.readVarInt(), buf.readNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeNbt(data);
    }

    public static void handle(PacketContext<EntityDataSyncPacket> ctx) {
        if (!Side.CLIENT.equals(ctx.side())) {
            return;
        }

        EntityDataSyncClientHandler.apply(ctx.message().entityId, ctx.message().data);
    }

    public static void syncToTrackers(Entity entity) {
        if (entity.level().isClientSide()) {
            return;
        }

        CompoundTag data = Services.ENTITYDATA.getStored(entity);
        if (data.isEmpty()) {
            return;
        }

        Dispatcher.sendToClientsLoadingChunk(new EntityDataSyncPacket(entity.getId(), data), entity.level().getChunkAt(entity.blockPosition()));
    }

    public static void syncToPlayer(Entity entity, ServerPlayer player) {
        CompoundTag data = Services.ENTITYDATA.getStored(entity);
        if (data.isEmpty()) {
            return;
        }

        Dispatcher.sendToClient(new EntityDataSyncPacket(entity.getId(), data), player);
    }
}
