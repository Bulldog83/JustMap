package ru.bulldog.justmap.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.ChunkPos;

import ru.bulldog.justmap.map.data.RegionPos;

public class ClientNetworkHandler extends NetworkHandler {
	private Map<Integer, Consumer<?>> responseListeners = new HashMap<>();
	private boolean serverReady = false;
	private Random random;
	
	public void registerPacketsListener() {
		clientPacketRegistry.register(INIT_PACKET_ID, (context, data) -> {
			long seed = data.readLong();
			this.random = new Random(seed);
			this.serverReady = true;
		});
		clientPacketRegistry.register(CHANNEL_ID, (context, data) -> {
			PacketType packet_type = PacketType.get(data.readInt());
			
			switch(packet_type) {
				case ACTIVE_CHUNK_PACKET: {
					context.getTaskQueue().execute(() -> this.onChunkActiveResponse(data));
					break;
				}
				case GET_IMAGE_PACKET: {
					context.getTaskQueue().execute(() -> this.onRegionImageResponse(data));
					break;
				}
				case SLIME_CHUNK_PACKET: {
					context.getTaskQueue().execute(() -> this.onChunkHasSlimeResponse(data));
					break;
				}
			}
		});
	}
	
	public boolean canRequestData() {
		return serverReady;
	}
	
	public void requestChunkHasSlime(ChunkPos chunkPos, Consumer<Boolean> responseConsumer) {
		this.registerResponseConsumer(responseConsumer);
	}
	
	private void onChunkHasSlimeResponse(PacketByteBuf data) {
		
	}
	
	public void requestChunkActive(ChunkPos chunkPos, Consumer<Boolean> responseConsumer) {
		this.registerResponseConsumer(responseConsumer);
	}
	
	private void onChunkActiveResponse(PacketByteBuf data) {
		
	}
	
	public void requestRegionImage(RegionPos regionPos, Consumer<byte[]> responseConsumer) {
		this.registerResponseConsumer(responseConsumer);
	}
	
	private void onRegionImageResponse(PacketByteBuf data) {
		
	}
	
	private void registerResponseConsumer(Consumer<?> responseConsumer) {
		int request_id = random.nextInt();
		this.responseListeners.put(request_id, responseConsumer);
	}
}
