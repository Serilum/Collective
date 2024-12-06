package com.natamus.collective.fabric.networking;

import com.natamus.collective.data.Constants;
import com.natamus.collective.implementations.networking.PacketRegistrationHandler;
import com.natamus.collective.implementations.networking.data.CommonPacketWrapper;
import com.natamus.collective.implementations.networking.data.PacketContainer;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.implementations.networking.exceptions.RegistrationException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.8
 *  by MysticDrew */

public class FabricNetworkHandler extends PacketRegistrationHandler {
    public FabricNetworkHandler(Side side) {
        super(side);
    }

    protected <T> void registerPacket(PacketContainer<T> container) {
        try {
            PayloadTypeRegistry.playC2S().register(container.getType(), container.getCodec());
            PayloadTypeRegistry.playS2C().register(container.getType(), container.getCodec());
        }
        catch (IllegalArgumentException ignored) { }

        if (Side.CLIENT.equals(this.side)) {
            Constants.LOG.debug("Registering packet {} : {} on the: {}", container.type().id(), container.classType(), Side.CLIENT);

            ClientPlayNetworking.registerGlobalReceiver(container.getType(), (ClientPlayNetworking.PlayPayloadHandler<CommonPacketWrapper<T>>) (payload, context) -> context.client().execute(() -> container.handler().accept(new PacketContext<>(payload.packet(), side))));
        }

        Constants.LOG.debug("Registering packet {} : {} on the: {}", container.type().id(), container.classType(), Side.SERVER);
        ServerPlayNetworking.registerGlobalReceiver(container.getType(), (ServerPlayNetworking.PlayPayloadHandler<CommonPacketWrapper<T>>) (payload, context) -> context.player().server.execute(() -> container.handler().accept(new PacketContext<>(context.player(), payload.packet(), side))));

    }

    public <T> void sendToServer(T packet) {
        this.sendToServer(packet, false);
    }

    @SuppressWarnings("unchecked")
	public <T> void sendToServer(T packet, boolean ignoreCheck) {
        PacketContainer<T> container = (PacketContainer<T>) PACKET_MAP.get(packet.getClass());
        if (container != null) {
            if (ignoreCheck || ClientPlayNetworking.canSend(container.type().id())) {
                ClientPlayNetworking.send(new CommonPacketWrapper<>(container, packet));
            }
        }
        else {
            throw new RegistrationException(packet.getClass() + "{} packet not registered on the client, packets need to be registered on both sides!");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void sendToClient(T packet, ServerPlayer player) {
        PacketContainer<T> container = (PacketContainer<T>) PACKET_MAP.get(packet.getClass());
        if (container != null) {
            if (ServerPlayNetworking.canSend(player, container.type().id())) {
                ServerPlayNetworking.send(player, new CommonPacketWrapper<>(container, packet));
            }
        }
        else {
            throw new RegistrationException(packet.getClass() + "{} packet not registered on the server, packets need to be registered on both sides!");
        }
    }
}
