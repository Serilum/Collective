package com.natamus.collective.networking.packets;

import com.natamus.collective.config.DuskConfigServerConfig;
import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class ConfigServerRequestPacket {
	public static final ResourceLocation CHANNEL = ResourceLocation.fromNamespaceAndPath(CollectiveReference.MOD_ID, "config_server_request");

	public final String modid;

	public ConfigServerRequestPacket(String modid) {
		this.modid = modid;
	}

	public static ConfigServerRequestPacket decode(FriendlyByteBuf buf) {
		return new ConfigServerRequestPacket(buf.readUtf());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(modid);
	}

	public static void handle(PacketContext<ConfigServerRequestPacket> ctx) {
		if (!Side.SERVER.equals(ctx.side()) || !(ctx.sender() instanceof ServerPlayer player)) {
			return;
		}

		String modid = ctx.message().modid;
		List<String> parts = PacketParts.split(DuskConfigServerConfig.getServerConfig(player, modid), ConfigServerValuesPacket.partLength);
		for (int i = 0; i < parts.size(); i++) {
			boolean isLastPart = i == parts.size() - 1;
			Dispatcher.sendToClient(new ConfigServerValuesPacket(modid, parts.get(i), isLastPart), player);
		}
	}
}
