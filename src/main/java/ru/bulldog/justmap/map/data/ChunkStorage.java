package ru.bulldog.justmap.map.data;

import java.io.File;
import java.io.IOException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.tasks.TaskManager;

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
	public void close() {
		TaskManager storageDestroyer = TaskManager.getManager("storage-destroyer");
		storageDestroyer.execute("Storage: Destructing chunk storage...", () -> {
			JustMap.LOGGER.debug("Storage: Start storage destructing...");
			try {
				this.worker.completeAll().join();
				this.worker.close();
			} catch (Exception ex) {
				JustMap.LOGGER.catching(ex);
			}
			JustMap.LOGGER.debug("Storage: Chunk storage destructed!");
		});
		storageDestroyer.stop();
	}
}
