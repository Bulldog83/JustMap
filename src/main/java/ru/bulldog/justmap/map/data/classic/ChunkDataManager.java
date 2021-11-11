package ru.bulldog.justmap.map.data.classic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NbtCompound;
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
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.storage.StorageUtil;
import ru.bulldog.justmap.util.tasks.MemoryUtil;
import ru.bulldog.justmap.util.tasks.TaskManager;

class ChunkDataManager {
	private final static TaskManager chunkProcessor = TaskManager.getManager("chunk-processor");
	private final Map<ChunkPos, ChunkData> mapChunks = new HashMap<>();
	private final Set<ChunkPos> requestedChunks = new HashSet<>();
	private final WorldData mapData;
	private final WorldChunk emptyChunk;
	
	ChunkDataManager(WorldData data, World world) {
		this.emptyChunk = new EmptyChunk(world, new ChunkPos(0, 0));
		this.mapData = data;
	}

	ChunkData getChunk(ChunkPos chunkPos) {
		return this.getChunk(chunkPos.x, chunkPos.z);
	}
	
	ChunkData getChunk(int posX, int posZ) {
		ChunkPos chunkPos = new ChunkPos(posX, posZ);

		ChunkData mapChunk;
		synchronized (mapChunks) {
			if (this.hasChunk(chunkPos)) {
				mapChunk = this.mapChunks.get(chunkPos);
			} else {
				mapChunk = new ChunkData(mapData, chunkPos);
				this.mapChunks.put(chunkPos, mapChunk);
			}
		}		
		mapChunk.requested = System.currentTimeMillis();
		
		return mapChunk;
	}
	
	WorldChunk getEmptyChunk() {
		return this.emptyChunk;
	}
	
	boolean hasChunk(ChunkPos chunkPos) {
		return this.mapChunks.containsKey(chunkPos);
	}
	
	void purge(int maxPurged, int timeLimit) {
		long currentTime = System.currentTimeMillis();
		int purged = 0;
	
		synchronized (mapChunks) {
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
	}

	void clear() {
		synchronized (mapChunks) {
			this.mapChunks.clear();
		}
	}
	
	WorldChunk callSavedChunk(World world, ChunkPos chunkPos) {
		if (!(world instanceof ServerWorld)) return this.emptyChunk;
		if (requestedChunks.add(chunkPos)) {
			return (WorldChunk) chunkProcessor.run("Call saves for chunk " + chunkPos, (future) -> {
				return () -> {
					future.complete(this.callSaves(world, chunkPos));
					this.requestedChunks.remove(chunkPos);
				};
			}).join();
		}
		return this.emptyChunk;
	}
	
	private WorldChunk callSaves(World world, ChunkPos chunkPos) {
		long usedPct = MemoryUtil.getMemoryUsage();
		if (usedPct > 85L) {
			JustMap.LOGGER.warning("Not enough memory, can't load more chunks.");
			return this.emptyChunk;
        }
		
		ServerWorld serverWorld = (ServerWorld) world;
		try (VersionedChunkStorage storage = StorageUtil.getChunkStorage(serverWorld)) {
			NbtCompound chunkTag = storage.updateChunkNbt(serverWorld.getRegistryKey(),
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
