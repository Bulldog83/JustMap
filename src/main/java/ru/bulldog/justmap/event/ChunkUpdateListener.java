package ru.bulldog.justmap.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.util.math.Plane;
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
		if (!JustMapClient.canMapping()) {
			updateQueue.clear();
			return;
		}
		while(!updateQueue.isEmpty()) {
			ChunkUpdateEvent event = updateQueue.poll();
			if (event == null) break;
			event.mapChunk.updateWorldChunk(event.worldChunk);
			boolean accepted;
			if (event.full) {
				accepted = event.mapChunk.updateFullChunk(event.layer, event.level, event.update);
			} else {
				Plane area = event.updateArea;
				int x = (int) area.first.x;
				int z = (int) area.first.y;
				int w = (int) area.second.x;
				int h = (int) area.second.y;
				accepted = event.mapChunk.updateChunkArea(event.layer, event.level, event.update, x, z, w, h);
			}
			if(!accepted) {
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
