package ru.bulldog.justmap.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import ru.bulldog.justmap.JustMap;

public class NetworkHandler {
	public final static ResourceLocation CHANNEL_ID = new ResourceLocation(JustMap.MODID, "networking");
	public final static ResourceLocation INIT_PACKET_ID = new ResourceLocation(JustMap.MODID, "networking_init");

	protected final static ClientSidePacketRegistry clientPacketRegistry = ClientSidePacketRegistry.INSTANCE;
	protected final static ServerSidePacketRegistry serverPacketRegistry = ServerSidePacketRegistry.INSTANCE;

	public boolean canServerReceive() {
		return clientPacketRegistry.canServerReceive(CHANNEL_ID);
	}

	public boolean canPlayerReceive(Player player) {
		return serverPacketRegistry.canPlayerReceive(player, CHANNEL_ID);
	}
	
	public void sendToPlayer(Player player, Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> completionListener) {
		serverPacketRegistry.sendToPlayer(player, packet, completionListener);
	}
	
	public void sendToPlayer(Player player, Packet<?> packet) {
		sendToPlayer(player, packet, null);
	}
	
	public void sendToServer(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> completionListener) {
		clientPacketRegistry.sendToServer(packet, completionListener);
	}
	
	public void sendToServer(Packet<?> packet) {
		sendToServer(packet, null);
	}
	
	public static enum PacketType {
		SLIME_CHUNK_PACKET,
		GET_IMAGE_PACKET;
		
		private final static PacketType[] values = values();
		
		public static PacketType get(int id) {
			return values[id];
		}
	}
}
