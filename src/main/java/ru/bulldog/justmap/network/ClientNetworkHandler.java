package ru.bulldog.justmap.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.level.ChunkPos;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.data.RegionPos;

public class ClientNetworkHandler extends NetworkHandler {
	private Map<Integer, Consumer<?>> responseListeners = new HashMap<>();
	private boolean serverReady = false;
	private Random random;
	
	public void registerPacketsListeners() {
		clientPacketRegistry.register(INIT_PACKET_ID, (context, data) -> {
			long seed = data.readLong();
			this.random = new Random(seed);
			this.serverReady = true;
			JustMap.LOGGER.info("Networking successfully initialized.");
		});
		clientPacketRegistry.register(CHANNEL_ID, (context, data) -> {
			ByteBuf packetData = data.copy();
			PacketType packet_type = PacketType.get(packetData.readByte());
			switch(packet_type) {
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
		ServerboundCustomPayloadPacket packet = new ServerboundCustomPayloadPacket(CHANNEL_ID, data);
		this.sendToServer(packet, result -> {
			if (!result.isSuccess()) {
				this.responseListeners.remove(packet_id);
			}
		});
	}
	
	private void onChunkHasSlimeResponse(ByteBuf data) {
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
	
	private void onRegionImageResponse(ByteBuf data) {
		
	}
	
	private int registerResponseConsumer(Consumer<?> responseConsumer) {
		int request_id = random.nextInt();
		this.responseListeners.put(request_id, responseConsumer);
		return request_id;
	}
}
