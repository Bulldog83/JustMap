package ru.bulldog.justmap.map.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.storage.VersionedChunkStorage;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.tasks.MemoryUtil;

public class ChunkDataManager {
	private final Map<ChunkPos, ChunkData> mapChunks = new ConcurrentHashMap<>();
	private final DimensionData mapData;
	private World world;
	
	public ChunkDataManager(DimensionData data, World world) {
		this.mapData = data;
		this.world = world;
	}
	
	public void updateWorld(World world) {
		this.world = world;
	}

	public void addLoadedChunk(World world, WorldChunk lifeChunk) {
		if (world == null || lifeChunk == null || lifeChunk.isEmpty()) return;
		
		ChunkPos chunkPos = lifeChunk.getPos();
		if (mapChunks.containsKey(chunkPos)) {
			ChunkData mapChunk = mapChunks.get(chunkPos);
			mapChunk.updateWorldChunk(lifeChunk);
			mapChunk.update(mapData.getLayer(), mapData.getLevel(), true);
		} else {
			ChunkData mapChunk = new ChunkData(mapData, world, lifeChunk);
			mapChunk.update(mapData.getLayer(), mapData.getLevel(), true);
			this.mapChunks.put(chunkPos, mapChunk);
		}
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
	
	public static WorldChunk callSavedChunk(World world, ChunkPos chunkPos) {
		if (!(world instanceof ServerWorld)) return null;
		
		long usedPct = MemoryUtil.getMemoryUsage();
		if (usedPct > 85L) {
			JustMap.LOGGER.logWarning("Not enough memory, can't load/generate more chunks.");
			return null;
        }
		
		ServerWorld serverWorld = (ServerWorld) world;
		ServerChunkManager manager = serverWorld.getChunkManager();
		VersionedChunkStorage storage = manager.threadedAnvilChunkStorage;
		try {		
			CompoundTag chunkTag = storage.getNbt(chunkPos);
			if (chunkTag == null) return null;
			
			Chunk chunk = ChunkSerializer.deserialize(
					serverWorld, serverWorld.getStructureManager(), serverWorld.getPointOfInterestStorage(), chunkPos, chunkTag);
			if (chunk instanceof ReadOnlyChunk) {
				WorldChunk worldChunk = ((ReadOnlyChunk) chunk).getWrappedChunk();
				worldChunk.setLoadedToWorld(true);
				return worldChunk;
			}
			return null;
		} catch (Exception ex) {
			if (ex instanceof IOException) {
				JustMap.LOGGER.catching(ex);
			}
			return null;
		}
	}
}
