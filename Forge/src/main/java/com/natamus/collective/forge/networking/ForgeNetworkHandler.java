package com.natamus.collective.forge.networking;

import com.natamus.collective.data.Constants;
import com.natamus.collective.implementations.networking.PacketRegistrationHandler;
import com.natamus.collective.implementations.networking.data.CommonPacketWrapper;
import com.natamus.collective.implementations.networking.data.PacketContainer;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.implementations.networking.exceptions.RegistrationException;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.8
 *  by MysticDrew */

public class ForgeNetworkHandler extends PacketRegistrationHandler {
	private final Map<Class<?>, Message<?>> CHANNELS = new HashMap<>();

	public ForgeNetworkHandler(Side side) {
		super(side);
	}

	@SuppressWarnings("unchecked")
	protected <T> void registerPacket(PacketContainer<T> container) {
		if (CHANNELS.get(container.classType()) == null) {
			var channel = ChannelBuilder.named(container.type().id()).optional().payloadChannel()
					.play()
					.bidirectional()
					.addMain(container.getType(), container.getCodec(), (msg, ctx) ->
							buildHandler(container.handler()).accept((T) msg.packet(), ctx))
					.build();
			CHANNELS.put(container.classType(), new Message<>(channel, container));
		}
	}

	public <T> void sendToServer(T packet) {
		this.sendToServer(packet, false);
	}

	@SuppressWarnings("unchecked")
	public <T> void sendToServer(T packet, boolean ignoreCheck) {
		var message = (Message<T>) CHANNELS.get(packet.getClass());
		if (message != null) {
			var channel = message.channel();
			Connection connection = Minecraft.getInstance().getConnection().getConnection();

			if (ignoreCheck || channel.isRemotePresent(connection)) {
				channel.send(new CommonPacketWrapper<>(message.container(), packet), connection);
			}
		}
		else {
			throw new RegistrationException(packet.getClass() + "{} packet not registered on the client, packets need to be registered on both sides!");
		}
	}

	@SuppressWarnings("unchecked")
	public <T> void sendToClient(T packet, ServerPlayer player) {
		var message = (Message<T>) CHANNELS.get(packet.getClass());
		if (message != null) {
			var channel = message.channel();
			Connection connection = player.connection.getConnection();

			if (channel.isRemotePresent(connection)) {
				channel.send(new CommonPacketWrapper<>(message.container(), packet), connection);
			}
			else {
				throw new RegistrationException(packet.getClass() + "{} packet not registered on the server, packets need to be registered on both sides!");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> boolean isRegisteredOnClient(Class<T> packetClass, ServerPlayer player) {
		var message = (Message<T>) CHANNELS.get(packetClass);
		return message != null && message.channel().isRemotePresent(player.connection.getConnection());
	}

	private <T> BiConsumer<T, CustomPayloadEvent.Context> buildHandler(Consumer<PacketContext<T>> handler) {
		return (message, ctx) -> {
			ctx.setPacketHandled(true);
			ctx.enqueueWork(() -> {
				try {
					Side side = ctx.isServerSide() ? Side.SERVER : Side.CLIENT;
					ServerPlayer player = ctx.getSender();
					handler.accept(new PacketContext<>(player, message, side));
				} catch (Throwable t) {
					Constants.LOG.error("{} error handling packet", message.getClass(), t);
				}
			});
		};
	}

	public record Message<T>(Channel<CustomPacketPayload> channel, PacketContainer<T> container) { }
}