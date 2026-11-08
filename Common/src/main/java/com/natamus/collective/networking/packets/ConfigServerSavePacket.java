package com.natamus.collective.networking.packets;

import com.natamus.collective.config.DuskConfigServerConfig;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class ConfigServerSavePacket {
	public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(CollectiveReference.MOD_ID, "config_server_save");
	public static final int partLength = 10000;

	public final String modid;
	public final String changesPart;
	public final boolean isFirstPart;
	public final boolean isLastPart;

	public ConfigServerSavePacket(String modid, String changesPart, boolean isFirstPart, boolean isLastPart) {
		this.modid = modid;
		this.changesPart = changesPart;
		this.isFirstPart = isFirstPart;
		this.isLastPart = isLastPart;
	}

	public static ConfigServerSavePacket decode(FriendlyByteBuf buf) {
		return new ConfigServerSavePacket(buf.readUtf(), buf.readUtf(partLength), buf.readBoolean(), buf.readBoolean());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(modid);
		buf.writeUtf(changesPart, partLength);
		buf.writeBoolean(isFirstPart);
		buf.writeBoolean(isLastPart);
	}

	public static void handle(PacketContext<ConfigServerSavePacket> ctx) {
		if (!Side.SERVER.equals(ctx.side()) || !(ctx.sender() instanceof ServerPlayer player)) {
			return;
		}

		DuskConfigServerConfig.receiveChangesPart(player, ctx.message().modid, ctx.message().changesPart, ctx.message().isFirstPart, ctx.message().isLastPart);
	}
}
