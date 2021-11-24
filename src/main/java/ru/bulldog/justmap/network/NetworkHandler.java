package ru.bulldog.justmap.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;

import ru.bulldog.justmap.JustMap;

public class NetworkHandler {
	public final static Identifier CHANNEL_ID = new Identifier(JustMap.MODID, "networking");
	public final static Identifier INIT_PACKET_ID = new Identifier(JustMap.MODID, "networking_init");

	protected final static ClientSidePacketRegistry clientPacketRegistry = ClientSidePacketRegistry.INSTANCE;
	protected final static ServerSidePacketRegistry serverPacketRegistry = ServerSidePacketRegistry.INSTANCE;

	public boolean canServerReceive() {
		return clientPacketRegistry.canServerReceive(CHANNEL_ID);
	}

	public boolean canPlayerReceive(PlayerEntity player) {
		return serverPacketRegistry.canPlayerReceive(player, CHANNEL_ID);
	}

	public void sendToPlayer(PlayerEntity player, Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> completionListener) {
		serverPacketRegistry.sendToPlayer(player, packet, completionListener);
	}

	public void sendToPlayer(PlayerEntity player, Packet<?> packet) {
		sendToPlayer(player, packet, null);
	}

	public void sendToServer(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> completionListener) {
		clientPacketRegistry.sendToServer(packet, completionListener);
	}

	public void sendToServer(Packet<?> packet) {
		sendToServer(packet, null);
	}

	public enum PacketType {
		SLIME_CHUNK_PACKET,
		GET_IMAGE_PACKET;

		private final static PacketType[] values = values();

		public static PacketType get(int id) {
			return values[id];
		}
	}
}
