package ru.bulldog.justmap.map.data;

import java.io.File;
import java.io.IOException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;

public class ChunkStorage implements AutoCloseable {
	
	private StorageWorker worker;
	
	public ChunkStorage() {
		this.worker = new StorageWorker();
	}
	
	public CompoundTag getNbt(File dir, ChunkPos chunkPos) throws IOException {
		return this.worker.getNbt(dir, chunkPos);
	}

	public void setTagAt(File dir, ChunkPos chunkPos, CompoundTag compoundTag) {
		this.worker.setResult(dir, chunkPos, compoundTag);
	}

	@Override
	public void close() throws Exception {
		this.worker.close();
	}
}
