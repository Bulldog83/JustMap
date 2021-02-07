package ru.bulldog.justmap.fabric.network;

import io.netty.buffer.ByteBuf;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class FabricClientNetworkHandler extends ClientNetworkHandler {

	protected final static ClientSidePacketRegistry clientPacketRegistry = ClientSidePacketRegistry.INSTANCE;
	private Random random;
	private boolean serverReady;

	public void registerPacketsListeners() {
		clientPacketRegistry.register(INIT_PACKET_ID, (context, data) -> {
			long seed = data.readLong();
			this.random = new Random(seed);
			this.serverReady = true;
			JustMap.LOGGER.info("Networking successfully initialized.");
		});
		clientPacketRegistry.register(CHANNEL_ID, (context, data) -> {
			ByteBuf packetData = data.copy();
			PacketType packetType = PacketType.get(packetData.readByte());
			switch(packetType) {
				case SLIME_CHUNK_PACKET: {
					context.getTaskQueue().execute(() -> this.onChunkHasSlimeResponse(packetData));
					break;
				}
				case GET_IMAGE_PACKET: {
					context.getTaskQueue().execute(() -> this.onRegionImageResponse(packetData));
					break;
				}
			}
		});
	}

	public boolean canServerReceive() {
		return clientPacketRegistry.canServerReceive(CHANNEL_ID);
	}

	public void sendToServer(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> completionListener) {
		clientPacketRegistry.sendToServer(packet, completionListener);
	}
}
