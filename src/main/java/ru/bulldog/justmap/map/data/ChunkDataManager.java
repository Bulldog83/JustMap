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
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.storage.VersionedChunkStorage;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.tasks.MemoryUtil;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class ChunkDataManager {
	private final static TaskManager chunkGenerator = TaskManager.getManager("chunk-processor", 1024);

	private final Map<ChunkPos, ChunkData> mapChunks = new ConcurrentHashMap<>();
	private World world;
	
	public ChunkDataManager(World world) {
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
			ChunkData.updadeChunk(mapChunk);
		} else {
			ChunkData mapChunk = new ChunkData(world, lifeChunk, DimensionData.currentLayer(), DimensionData.currentLevel());
			ChunkData.updadeChunk(mapChunk);
			this.mapChunks.put(chunkPos, mapChunk);
		}
	}
	
	public ChunkData getChunk(Layer.Type layer, int level, int posX, int posZ) {
		ChunkPos chunkPos = new ChunkPos(posX, posZ);

		ChunkData mapChunk;
		if (mapChunks.containsKey(chunkPos)) {
			mapChunk = this.mapChunks.get(chunkPos);
			mapChunk.updateWorld(world);
		} else {
			mapChunk = new ChunkData(world, chunkPos, layer, level);
			this.mapChunks.put(chunkPos, mapChunk);
		}
		
		mapChunk.setLevel(layer, level);
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
	
	public static boolean callSavedChunk(World world, ChunkData mapChunk) {
		if (!(world instanceof ServerWorld)) return false;
		
		long usedPct = MemoryUtil.getMemoryUsage();
		if (usedPct > 85L) {
			JustMap.LOGGER.logWarning("Not enough memory, can't load/generate more chunks.");
			return false;
        }
		
		ServerWorld serverWorld = (ServerWorld) world;
		ServerChunkManager manager = serverWorld.getChunkManager();
		VersionedChunkStorage storage = manager.threadedAnvilChunkStorage;
		ChunkPos chunkPos = mapChunk.getPos();
		try {		
			CompoundTag chunkTag = storage.getNbt(chunkPos);
			if (chunkTag == null) return false;
			
			Chunk chunk = ChunkSerializer.deserialize(
					serverWorld, serverWorld.getStructureManager(), serverWorld.getPointOfInterestStorage(), chunkPos, chunkTag);
			if (chunk instanceof ReadOnlyChunk) {
				WorldChunk worldChunk = ((ReadOnlyChunk) chunk).getWrappedChunk();
				worldChunk.setLoadedToWorld(true);
				mapChunk.updateWorldChunk(worldChunk);
				return true;
			}
			if (ClientParams.chunksGeneration && chunkGenerator.canExecute()) {
				chunkGenerator.execute("Generating Chunk " + chunkPos,
						() -> ChunkDataManager.generateWorldChunk(manager, mapChunk));
			}
			return false;
		} catch (IOException ex) {
			JustMap.LOGGER.catching(ex);
			return false;
		}
	}
	
	public static void generateWorldChunk(ServerChunkManager manager, ChunkData mapChunk) {
		try {
			Chunk worldChunk = manager.getChunk(mapChunk.getX(), mapChunk.getZ(), ChunkStatus.FULL, true);
			if (worldChunk instanceof WorldChunk) {
				WorldChunk currentChunk = mapChunk.getWorldChunk();
				if (currentChunk == null || currentChunk.isEmpty()) {
					mapChunk.updateWorldChunk((WorldChunk) worldChunk);
				}
			}
		} catch (Exception ex) {
			JustMap.LOGGER.catching(ex);
		}	
	}
}
