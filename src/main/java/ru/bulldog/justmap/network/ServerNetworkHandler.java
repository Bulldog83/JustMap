package ru.bulldog.justmap.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkRandom;

import ru.bulldog.justmap.util.DimensionUtil;
import ru.bulldog.justmap.util.RuleUtil;

public class ServerNetworkHandler extends NetworkHandler {
	private MinecraftServer server;
	
	public ServerNetworkHandler(MinecraftServer server) {
		this.server = server;
	}
	
	public void onPlayerConnect(ServerPlayerEntity player) {
		PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
		ServerWorld world = server.getWorld(DimensionType.OVERWORLD);
		data.writeLong(world.getSeed());
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(INIT_PACKET_ID, data);
		this.sendToPlayer(player, packet);
	}
	
	public void registerPacketsListeners() {
		serverPacketRegistry.register(CHANNEL_ID, (context, data) -> {
			ByteBuf packetData = data.copy();
			PacketType packet_type = PacketType.get(packetData.readByte());
			ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
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
	
	private void onRegionImageRequest(ServerPlayerEntity player, ByteBuf data) {
		
	}
	
	private void onChunkHasSlimeRequest(ServerPlayerEntity player, ByteBuf data) {
		if (!canPlayerReceive(player)) return;
		int packet_id = data.readInt();
		int x = data.readInt();
		int z = data.readInt();
		
		boolean slime = false;
		if (RuleUtil.allowSlimeChunks() && DimensionUtil.isOverworld(player.world.dimension)) {
			ServerWorld world = player.getServerWorld();
			slime = ChunkRandom.create(x, z, world.getSeed(), 987234911L).nextInt(10) == 0;
		}
		PacketByteBuf response = new PacketByteBuf(Unpooled.buffer());
		response.writeByte(PacketType.SLIME_CHUNK_PACKET.ordinal());
		response.writeInt(packet_id);
		response.writeBoolean(slime);
		CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(CHANNEL_ID, response);
		this.sendToPlayer(player, packet);
	}
}
