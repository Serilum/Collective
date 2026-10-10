package com.natamus.collective.implementations.networking;

import com.natamus.collective.implementations.networking.data.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.6
 *  by MysticDrew */

public class NetworkSetup {
	private final PacketRegistrationHandler packetRegistration;
	private static final DelayedPacketRegistrationHandler delayedHandler = new DelayedPacketRegistrationHandler();
	public static volatile NetworkSetup INSTANCE;

	// Mods are constructed in parallel on (Neo)Forge, so registering and setting up share one lock.
	public NetworkSetup(PacketRegistrationHandler packetRegistration) {
		this.packetRegistration = packetRegistration;

		synchronized (NetworkSetup.class) {
			INSTANCE = this;
			delayedHandler.registerQueuedPackets(packetRegistration);
		}
	}

	/**
	 * Fabric does not enforce load order, so we may have to delay packet registrations.
	 *
	 * @return the handler;
	 */
	public static DelayedPacketRegistrationHandler getDelayedHandler() {
		return delayedHandler;
	}

	public static <T> PacketRegistrar registerPacket(ResourceLocation packetIdentifier, Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, Consumer<PacketContext<T>> handler) {
		synchronized (NetworkSetup.class) {
			if (INSTANCE != null) {
				return INSTANCE.packetRegistration.registerPacket(packetIdentifier, messageType, encoder, decoder, handler);
			}
			else {
				return delayedHandler.registerPacket(packetIdentifier, messageType, encoder, decoder, handler);
			}
		}
	}

	public PacketRegistrationHandler getPacketRegistration() {
		return packetRegistration;
	}
}
