package ru.bulldog.justmap.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.world.level.ChunkPos;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.map.data.RegionPos;

public abstract class ClientNetworkHandler extends NetworkHandler {
	protected final Map<Integer, Consumer<?>> responseListeners = Maps.newHashMap();
	protected boolean serverReady = false;
	protected Random random;
	
	public boolean canRequestData() {
		return serverReady;
	}
	
	public void requestChunkHasSlime(ChunkPos chunkPos, Consumer<Boolean> responseConsumer) {
		if (!canServerReceive()) return;
		int packet_id = this.registerResponseConsumer(responseConsumer);
		FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
		data.writeByte(PacketType.SLIME_CHUNK_PACKET.ordinal());
		data.writeInt(packet_id);
		data.writeInt(chunkPos.x);
		data.writeInt(chunkPos.z);
		ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(CHANNEL_ID, data);
		this.sendToServer(packet, result -> {
			if (!result.isSuccess()) {
				this.responseListeners.remove(packet_id);
			}
		});
	}
	
	protected void onChunkHasSlimeResponse(ByteBuf data) {
		int packet_id = data.readInt();
		boolean result = data.readBoolean();
		if (responseListeners.containsKey(packet_id)) {
			@SuppressWarnings("unchecked")
			Consumer<Boolean> responseConsumer = (Consumer<Boolean>) responseListeners.get(packet_id);
			responseConsumer.accept(result);
		}
	}
	
	public void requestRegionImage(RegionPos regionPos, Consumer<byte[]> responseConsumer) {
		this.registerResponseConsumer(responseConsumer);
	}
	
	protected void onRegionImageResponse(ByteBuf data) {
		
	}
	
	protected int registerResponseConsumer(Consumer<?> responseConsumer) {
		int request_id = random.nextInt();
		this.responseListeners.put(request_id, responseConsumer);
		return request_id;
	}
}
