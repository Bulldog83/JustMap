package ru.bulldog.justmap.fabric.network;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class FabricServerNetworkHandler extends ServerNetworkHandler {

	protected final static ServerSidePacketRegistry serverPacketRegistry = ServerSidePacketRegistry.INSTANCE;

	public void registerPacketsListeners() {
		serverPacketRegistry.register(CHANNEL_ID, (context, data) -> {
			ByteBuf packetData = data.copy();
			PacketType packet_type = PacketType.get(packetData.readByte());
			ServerPlayer player = (ServerPlayer) context.getPlayer();
			switch(packet_type) {
				case GET_IMAGE_PACKET: {
					context.getTaskQueue().execute(() -> this.onRegionImageRequest(player, packetData));
					break;
				}
				case SLIME_CHUNK_PACKET: {
					context.getTaskQueue().execute(() -> this.onChunkHasSlimeRequest(player, packetData));
					break;
				}
			}
		});
	}

	public void sendToPlayer(Player player, Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> completionListener) {
		serverPacketRegistry.sendToPlayer(player, packet, completionListener);
	}

	public boolean canPlayerReceive(Player player) {
		return serverPacketRegistry.canPlayerReceive(player, CHANNEL_ID);
	}
}
