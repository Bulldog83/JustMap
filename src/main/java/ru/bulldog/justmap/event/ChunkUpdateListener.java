package ru.bulldog.justmap.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import ru.bulldog.justmap.util.tasks.TaskManager;

public class ChunkUpdateListener {
	private static Queue<ChunkUpdateEvent> updateQueue = new ConcurrentLinkedQueue<>();
	private static TaskManager worker = TaskManager.getManager("chunk-update-listener");
	
	public static void accept(ChunkUpdateEvent event) {
		if (updateQueue.contains(event)) return;
		updateQueue.offer(event);
	}
	
	private static void updateChunks() {
		if (updateQueue.isEmpty()) return;
		while(!updateQueue.isEmpty()) {
			ChunkUpdateEvent event = updateQueue.poll();
			event.mapChunk.updateWorldChunk(event.worldChunk);
			if(!event.mapChunk.update(event.layer, event.level, true)) {
				accept(event);
			}
		}
	}
	
	public static void proceed() {
		if (updateQueue.isEmpty() || worker.queueSize() > 0) return;
		worker.execute(ChunkUpdateListener::updateChunks);
	}
	
	public static void stop() {
		updateQueue.clear();
	}
}
