package ru.bulldog.justmap.map.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;

public class ChunkStorage implements AutoCloseable {
	
	private final Map<ChunkPos, CompoundTag> cashedChunks;
	private final StorageWorker worker;
	
	public ChunkStorage(File file) {
		this.worker = new StorageWorker(new RegionStorage(file));
		this.cashedChunks = new HashMap<>();
	}
	
	public CompoundTag getNbt(ChunkPos chunkPos) throws IOException {
		if(cashedChunks.containsKey(chunkPos)) {
			return cashedChunks.get(chunkPos);
		}
		
		CompoundTag chunkTag = this.worker.getNbt(chunkPos);
		cashedChunks.put(chunkPos, chunkTag);
		
		return chunkTag;
	}

	public void setTagAt(ChunkPos chunkPos, CompoundTag compoundTag) {
		if (cashedChunks.containsKey(chunkPos)) {
			cashedChunks.replace(chunkPos, compoundTag);
		}
		
		this.worker.setResult(chunkPos, compoundTag);
	}

	public void completeAll() {
		this.worker.completeAll().join();
	}

	@Override
	public void close() throws Exception {
		this.cashedChunks.clear();
		this.worker.close();
	}
}
