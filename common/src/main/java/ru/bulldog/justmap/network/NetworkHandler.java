package ru.bulldog.justmap.network;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import ru.bulldog.justmap.JustMap;

public abstract class NetworkHandler {
	public final static ResourceLocation CHANNEL_ID = new ResourceLocation(JustMap.MOD_ID, "networking");
	public final static ResourceLocation INIT_PACKET_ID = new ResourceLocation(JustMap.MOD_ID, "networking_init");

	public abstract void registerPacketsListeners();
	public abstract boolean canServerReceive();
	public abstract boolean canPlayerReceive(Player player);
	public abstract void sendToPlayer(Player player, Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> completionListener);
	public abstract void sendToServer(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> completionListener);

	public void sendToPlayer(Player player, Packet<?> packet) {
		sendToPlayer(player, packet, null);
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
