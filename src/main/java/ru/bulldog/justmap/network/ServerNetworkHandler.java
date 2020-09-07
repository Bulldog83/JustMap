package ru.bulldog.justmap.network;

import io.netty.buffer.Unpooled;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ServerNetworkHandler extends NetworkHandler {
	MinecraftServer server;
	
	public ServerNetworkHandler(MinecraftServer server) {
		this.server = server;
	}
	
	public void onPlayerConnect(ServerPlayerEntity player) {
		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		ServerWorld world = server.getWorld(player.getServerWorld().getRegistryKey());
		data.writeLong(world.getSeed());
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(INIT_PACKET_ID, data);
		this.sendToPlayer(player, packet);
	}
	
	public void registerPacketsListener() {
		
	}
}
