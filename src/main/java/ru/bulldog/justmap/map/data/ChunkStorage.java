package ru.bulldog.justmap.map.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;

public class ChunkStorage implements AutoCloseable {
	
	private Map<File, ChunksCache> chunksCache;
	private StorageWorker worker;
	
	public ChunkStorage() {
		this.worker = new StorageWorker();
		this.chunksCache = new HashMap<>();
	}
	
	public CompoundTag getNbt(File dir, ChunkPos chunkPos) throws IOException {
		if(chunksCache.containsKey(dir)) {
			ChunksCache cache = chunksCache.get(dir);
			if (cache.contains(chunkPos)) {
				return cache.get(chunkPos);
			}
			CompoundTag chunkTag = this.worker.getNbt(dir, chunkPos);
			cache.put(chunkPos, chunkTag);
			
			return chunkTag;
		}
		
		ChunksCache cache = new ChunksCache();
		CompoundTag chunkTag = this.worker.getNbt(dir, chunkPos);
		cache.put(chunkPos, chunkTag);
		this.chunksCache.put(dir, cache);
		
		return chunkTag;
	}

	public void setTagAt(File dir, ChunkPos chunkPos, CompoundTag compoundTag) {
		if(chunksCache.containsKey(dir)) {			
			this.chunksCache.get(dir).replace(chunkPos, compoundTag);
		}		
		this.worker.setResult(dir, chunkPos, compoundTag);
	}

	@Override
	public void close() throws Exception {
		this.chunksCache.clear();
		this.worker.close();
	}
	
	private class ChunksCache {
		Map<ChunkPos, CompoundTag> cachedChunks;
		
		ChunksCache() {
			this.cachedChunks = new HashMap<>();
		}
		
		CompoundTag get(ChunkPos pos) {
			return this.cachedChunks.get(pos);
		}
		
		void put(ChunkPos pos, CompoundTag tag) {
			this.cachedChunks.put(pos, tag);
		}
		
		void replace(ChunkPos pos, CompoundTag tag) {
			this.cachedChunks.replace(pos, tag);
		}
		
		boolean contains(ChunkPos pos) {
			return this.cachedChunks.containsKey(pos);
		}
	}
}
