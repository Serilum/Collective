package com.natamus.collective.networking.packets;

import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class ConfigSyncPacket {
	public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(CollectiveReference.MOD_ID, "config_sync");
	private static final int maxServerValuesLength = 262144;

	public final String modid;
	public final String serverValues;

	public ConfigSyncPacket(String modid, String serverValues) {
		this.modid = modid;
		this.serverValues = serverValues;
	}

	public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
		return new ConfigSyncPacket(buf.readUtf(), buf.readUtf(maxServerValuesLength));
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(modid);
		buf.writeUtf(serverValues, maxServerValuesLength);
	}

	public static void handle(PacketContext<ConfigSyncPacket> ctx) {
		if (!Side.CLIENT.equals(ctx.side())) {
			return;
		}

		ConfigSyncClientHandler.apply(ctx.message().modid, ctx.message().serverValues);
	}
}
