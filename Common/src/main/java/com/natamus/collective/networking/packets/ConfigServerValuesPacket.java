package com.natamus.collective.networking.packets;

import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class ConfigServerValuesPacket {
	public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(CollectiveReference.MOD_ID, "config_server_values");
	public static final int partLength = 100000;

	public final String modid;
	public final String serverConfigPart;
	public final boolean isLastPart;

	public ConfigServerValuesPacket(String modid, String serverConfigPart, boolean isLastPart) {
		this.modid = modid;
		this.serverConfigPart = serverConfigPart;
		this.isLastPart = isLastPart;
	}

	public static ConfigServerValuesPacket decode(FriendlyByteBuf buf) {
		return new ConfigServerValuesPacket(buf.readUtf(), buf.readUtf(partLength), buf.readBoolean());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(modid);
		buf.writeUtf(serverConfigPart, partLength);
		buf.writeBoolean(isLastPart);
	}

	public static void handle(PacketContext<ConfigServerValuesPacket> ctx) {
		if (!Side.CLIENT.equals(ctx.side())) {
			return;
		}

		ConfigServerClientHandler.receiveServerConfigPart(ctx.message().modid, ctx.message().serverConfigPart, ctx.message().isLastPart);
	}
}
