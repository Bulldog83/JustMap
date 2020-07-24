package ru.bulldog.justmap.map.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.storage.VersionedChunkStorage;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.event.ChunkUpdateEvent;
import ru.bulldog.justmap.event.ChunkUpdateListener;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.storage.StorageUtil;
import ru.bulldog.justmap.util.tasks.MemoryUtil;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class ChunkDataManager {
	private final static TaskManager chunkProcessor = TaskManager.getManager("chunk-processor");
	private final Map<ChunkPos, ChunkData> mapChunks = new ConcurrentHashMap<>();
	private final Set<ChunkPos> requestedChunks = new HashSet<>();
	private final DimensionData mapData;
	private final WorldChunk emptyChunk;
	private World world;
	
	public ChunkDataManager(DimensionData data, World world) {
		this.emptyChunk = new EmptyChunk(world, new ChunkPos(0, 0));
		this.mapData = data;
		this.world = world;
	}
	
	public void updateWorld(World world) {
		this.world = world;
	}

	public ChunkData getChunk(ChunkPos chunkPos) {
		return this.getChunk(chunkPos.x, chunkPos.z);
	}
	
	public ChunkData getChunk(int posX, int posZ) {
		ChunkPos chunkPos = new ChunkPos(posX, posZ);

		ChunkData mapChunk;
		if (mapChunks.containsKey(chunkPos)) {
			mapChunk = this.mapChunks.get(chunkPos);
			mapChunk.updateWorld(world);
		} else {
			mapChunk = new ChunkData(mapData, world, chunkPos);
			this.mapChunks.put(chunkPos, mapChunk);
		}
		mapChunk.requested = System.currentTimeMillis();
		
		return mapChunk;
	}
	
	public WorldChunk getEmptyChunk() {
		return this.emptyChunk;
	}
	
	public void purge(int maxPurged, int timeLimit) {
		long currentTime = System.currentTimeMillis();
		int purged = 0;
	
		List<ChunkPos> chunks = new ArrayList<>();
		for (ChunkPos chunkPos : this.mapChunks.keySet()) {
			ChunkData chunkData = this.mapChunks.get(chunkPos);
			if (currentTime - chunkData.requested >= timeLimit) {
				chunks.add(chunkPos);
				purged++;
				if (purged >= maxPurged) {
					break;
				}
			}
		}
	
		for (ChunkPos chunkPos : chunks) {
			this.mapChunks.remove(chunkPos);
		}
	}

	public void clear() {
		this.mapChunks.clear();
	}
	
	public void callSavedChunk(World world, ChunkPos chunkPos, Layer layer, int level) {
		if (!(world instanceof ServerWorld)) return;
		if (requestedChunks.add(chunkPos)) {
			chunkProcessor.execute("Call saves for chunk " + chunkPos, () -> {
				WorldChunk worldChunk = this.callSaves(world, chunkPos);
				if (!worldChunk.isEmpty()) {
					ChunkData mapChunk = this.getChunk(chunkPos);
					ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, true));
				}
				this.requestedChunks.remove(chunkPos);
			});
		}
	}
	
	private WorldChunk callSaves(World world, ChunkPos chunkPos) {
		long usedPct = MemoryUtil.getMemoryUsage();
		if (usedPct > 85L) {
			JustMap.LOGGER.logWarning("Not enough memory, can't load more chunks.");
			return this.emptyChunk;
        }
		
		ServerWorld serverWorld = (ServerWorld) world;
		try (VersionedChunkStorage storage = StorageUtil.getChunkStorage(serverWorld);) {		
			CompoundTag chunkTag = storage.updateChunkTag(serverWorld.getRegistryKey(),
					DataUtil.getPersistentSupplier(), storage.getNbt(chunkPos));
			if (chunkTag == null) return this.emptyChunk;
			Chunk chunk = ChunkSerializer.deserialize(
					serverWorld, serverWorld.getStructureManager(), serverWorld.getPointOfInterestStorage(), chunkPos, chunkTag);
			if (chunk instanceof ReadOnlyChunk) {
				return ((ReadOnlyChunk) chunk).getWrappedChunk();
			}
			return this.emptyChunk;
		} catch (Exception ex) {
			if (ex instanceof IOException) {
				JustMap.LOGGER.catching(ex);
			}
			return this.emptyChunk;
		}
	}
}
