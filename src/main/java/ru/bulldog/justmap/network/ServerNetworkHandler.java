package ru.bulldog.justmap.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.RuleUtil;

public class ServerNetworkHandler extends NetworkHandler {
	private MinecraftServer server;
	
	public ServerNetworkHandler(MinecraftServer server) {
		this.server = server;
	}
	
	public void onPlayerConnect(ServerPlayer player) {
		FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
		ServerLevel world = server.getLevel(Level.OVERWORLD);
		data.writeLong(world.getSeed());
		ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(INIT_PACKET_ID, data);
		this.sendToPlayer(player, packet);
	}
	
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
	
	private void onRegionImageRequest(ServerPlayer player, ByteBuf data) {
		
	}
	
	private void onChunkHasSlimeRequest(ServerPlayer player, ByteBuf data) {
		if (!canPlayerReceive(player)) return;
		int packet_id = data.readInt();
		int x = data.readInt();
		int z = data.readInt();
		
		boolean slime = false;
		if (RuleUtil.allowSlimeChunks() && Dimension.isOverworld(player.level)) {
			ServerLevel world = player.getLevel();
			slime = WorldgenRandom.seedSlimeChunk(x, z, world.getSeed(), 987234911L).nextInt(10) == 0;
		}
		FriendlyByteBuf response = new FriendlyByteBuf(Unpooled.buffer());
		response.writeByte(PacketType.SLIME_CHUNK_PACKET.ordinal());
		response.writeInt(packet_id);
		response.writeBoolean(slime);
		ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(CHANNEL_ID, response);
		this.sendToPlayer(player, packet);
	}
}
