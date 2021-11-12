package ru.bulldog.justmap.map.data.classic.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.classic.ChunkData;
import ru.bulldog.justmap.map.data.classic.WorldData;
import ru.bulldog.justmap.map.data.classic.WorldManager;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.math.Plane;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class ChunkUpdateListener {
	private static final Queue<ChunkUpdateEvent> updateQueue = new ConcurrentLinkedQueue<>();
	private static final TaskManager worker = TaskManager.getManager("chunk-update-listener");

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
		while (!updateQueue.isEmpty()) {
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

	public static void onSetBlockState(BlockPos pos, BlockState state, World world) {
		WorldChunk worldChunk = world.getWorldChunk(pos);
		if (!worldChunk.isEmpty()) {
			IMap map = DataUtil.getCurrentlyShownMap();
			Layer layer = DataUtil.getLayer(world, pos);
			int level = DataUtil.getLevel(layer, pos.getY());
			if (layer.equals(map.getLayer()) && level == map.getLevel()) {
				WorldData mapData = WorldManager.WORLD_MANAGER.getWorldData();
				if (mapData == null) return;
				ChunkPos chunkPos = worldChunk.getPos();
				int chunkX = chunkPos.x;
				int chunkZ = chunkPos.z;
				int x = (pos.getX() - chunkX) - 1;
				int z = (pos.getZ() - chunkZ) - 1;
				if (x < 0 && z < 0) {
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ, 0, 0, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX - 1, chunkZ, 14, 0, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ - 1, 0, 14, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX - 1, chunkZ - 1, 14, 14, 2, 2);
				} else if (x < 0 && z > 13) {
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ, 0, 14, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX - 1, chunkZ, 14, 14, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ + 1, 0, 0, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX - 1, chunkZ + 1, 14, 0, 2, 2);
				} else if (x > 13 && z < 0) {
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ, 14, 0, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX + 1, chunkZ, 0, 0, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ - 1, 14, 14, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX + 1, chunkZ - 1, 0, 14, 2, 2);
				} else if (x > 13 && z > 13) {
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ, 14, 14, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX + 1, chunkZ, 0, 14, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ + 1, 14, 0, 2, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX + 1, chunkZ + 1, 0, 0, 2, 2);
				} else if (x < 0) {
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ, 0, z, 2, 3);
					updateChunk(mapData, worldChunk, layer, level, chunkX - 1, chunkZ, 14, z, 2, 3);
				} else if (x > 13) {
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ, 14, z, 2, 3);
					updateChunk(mapData, worldChunk, layer, level, chunkX + 1, chunkZ, 0, z, 2, 3);
				} else if (z < 0) {
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ, x, 0, 3, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ - 1, x, 14, 3, 2);
				} else if (z > 13) {
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ, x, 14, 3, 2);
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ + 1, x, 0, 3, 2);
				} else {
					updateChunk(mapData, worldChunk, layer, level, chunkX, chunkZ, x, z, 3, 3);
				}
			}
		}
	}

	private static void updateChunk(WorldData mapData, WorldChunk worldChunk, Layer layer, int level, int chx, int chz, int x, int z, int w, int h) {
		if (worldChunk.isEmpty()) return;
		ChunkData mapChunk = mapData.getChunk(worldChunk.getPos());
		ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, x, z, w, h, true));
	}
}
