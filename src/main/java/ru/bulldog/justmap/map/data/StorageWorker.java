package ru.bulldog.justmap.map.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class StorageWorker implements AutoCloseable {

	private final Map<File, RegionStorage> storages;
	private final Map<ChunkPos, Result> results = Maps.newLinkedHashMap();
	private final TaskManager worker = TaskManager.getManager("chunk-io");
	private final AtomicBoolean closed = new AtomicBoolean();
	
	private CompletableFuture<Void> future = new CompletableFuture<>();
	
	StorageWorker() {
		this.storages = new HashMap<>();
	}
	
	private RegionStorage getStorage(File dir) {
		if (storages.containsKey(dir)) {
			return storages.get(dir);
		}
		
		RegionStorage storage = new RegionStorage(dir);
		storages.put(dir, storage);
		
		return storage;
	}

	public void setResult(File dir, ChunkPos chunkPos, CompoundTag compoundTag) {
		this.worker.run("Storage: Writing chunk data for: " + chunkPos, (completableFuture) -> {
			return () -> {
				Result result = (Result) this.results.computeIfAbsent(chunkPos, (chunkPosx) -> {
					return new Result();
				});
				result.dir = dir;
				result.nbt = compoundTag;
				result.future.whenComplete((var1, throwable) -> {
					if (throwable != null) {
						completableFuture.completeExceptionally(throwable);
					} else {
						completableFuture.complete(null);
					}
				});
			};
		});
		this.worker.execute("Storage: Storing chunk data for: " + chunkPos, this::writeResult);
	}

	public CompoundTag getNbt(File dir, ChunkPos chunkPos) throws IOException {
		CompletableFuture<?> completableFuture = this.worker.run("Storage: Get chunk NBT: " + chunkPos, (completableFuturex) -> {
			return () -> {
				Result result = (Result) this.results.get(chunkPos);
				if (result != null) {
					completableFuturex.complete(result.nbt);
				} else {
					try {
						CompoundTag compoundTag = this.getStorage(dir).getTagAt(chunkPos);
						completableFuturex.complete(compoundTag);
					} catch (Exception ex) {
						JustMap.LOGGER.logWarning("Failed to read chunk {}", chunkPos, ex);
						completableFuturex.completeExceptionally(ex);
					}
				}
			};
		});

		try {
			return (CompoundTag) completableFuture.join();
		} catch (CompletionException ex) {
			if (ex.getCause() instanceof IOException) {
				throw (IOException) ex.getCause();
			} else {
				throw ex;
			}
		}
	}

	private CompletableFuture<Void> shutdown() {
		return this.worker.run("Storage: Shutdown...", (completableFuture) -> {
			return () -> {
				this.future = completableFuture;
				this.writeAll();
				this.finish();
			};
		});
	}

	public CompletableFuture<Void> completeAll() {
		return this.worker.run("Storage: Complete all...", (completableFuture) -> {
			return () -> {
				CompletableFuture<?> completableFuture2 = CompletableFuture.allOf((CompletableFuture[]) this.results.values().stream().map((result) -> {
					return result.future;
				}).toArray((i) -> {
					return new CompletableFuture[i];
				}));
				completableFuture2.whenComplete((object, throwable) -> {
					completableFuture.complete(null);
				});
			};
		});
	}
	
	private boolean writeResult() {
		Iterator<Entry<ChunkPos, Result>> iterator = this.results.entrySet().iterator();
		if (!iterator.hasNext()) {
			return false;
		} else {
			Entry<ChunkPos, Result> entry = (Entry<ChunkPos, Result>) iterator.next();
			this.write((ChunkPos) entry.getKey(), (Result) entry.getValue());
			iterator.remove();
			
			return true;
		}
	}

	private void writeAll() {
		JustMap.LOGGER.debug("Storage: Start writing results...");
		this.results.forEach(this::write);
		this.results.clear();
		JustMap.LOGGER.debug("Storage: All results written!");
	}

	private void write(ChunkPos chunkPos, Result result) {
		try {
			this.getStorage(result.dir).write(chunkPos, result.nbt);
			result.future.complete(null);
		} catch (Exception ex) {
			JustMap.LOGGER.logError("Failed to store chunk {}", chunkPos, ex);
			result.future.completeExceptionally(ex);
		}
	}

	private void finish() {
		JustMap.LOGGER.debug("Storage: Start closing storages...");
		Exception error = new Exception();
		this.storages.forEach((dir, storage) -> {
			try {
				storage.close();
			} catch (Exception ex) {
				JustMap.LOGGER.logError("Failed to close storage", ex);
				error.addSuppressed(ex);
			}
		});		
		JustMap.LOGGER.debug("Storage: Storages closed!");
		if (error.getSuppressed().length > 0) {
			this.future.completeExceptionally(error);
		} else {
			this.future.complete(null);
		}
	}
	
	@Override
	public void close() throws Exception {
		if (this.closed.compareAndSet(false, true)) {
			try {
				this.shutdown().join();
				this.worker.stop();
			} catch (CompletionException ex) {
				if (ex.getCause() instanceof IOException) {
					throw (IOException) ex.getCause();
				} else {
					throw ex;
				}
			}
		}
	}
	
	private static class Result {
		private File dir;
		private CompoundTag nbt;
		private final CompletableFuture<Void> future;

		private Result() {
			this.future = new CompletableFuture<>();
		}
	}
}
