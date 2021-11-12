package ru.bulldog.justmap.map.data.classic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapDataManager;
import ru.bulldog.justmap.map.data.MapRegionProvider;
import ru.bulldog.justmap.map.multiworld.WorldKey;
import ru.bulldog.justmap.map.data.classic.event.ChunkUpdateEvent;
import ru.bulldog.justmap.map.data.classic.event.ChunkUpdateListener;
import ru.bulldog.justmap.map.multiworld.MultiworldManager;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.tasks.MemoryUtil;

public final class WorldManager implements MapDataManager {

	public static final WorldManager WORLD_MANAGER = new WorldManager();
	public final MultiworldManager multiworldManager = new MultiworldManager();

	public final Map<WorldKey, WorldData> worldsData = new HashMap<>();
	private boolean cacheClearing = false;

	public List<WorldKey> registeredWorlds() {
		return new ArrayList<>(worldsData.keySet());
	}

	public void onConfigUpdate() {
		multiworldManager.onConfigUpdate(this::clearData);
	}

	public void onWorldChanged(World world) {
		multiworldManager.onWorldChanged(world);
	}

	public void onWorldSpawnPosChanged(BlockPos newPos) {
		multiworldManager.onWorldSpawnPosChanged(newPos);
	}

	public void setCurrentWorldName(String name) {
		multiworldManager.setCurrentWorldName(name);
	}

	public WorldKey getWorldKey() {
		return multiworldManager.currentWorldKey;
	}

	public MapRegionProvider getMapRegionProvider() {
		return getData();
	}

	public WorldData getData() {
		if (multiworldManager.currentWorld == null || multiworldManager.currentWorldKey == null) return null;

		WorldData data;
		synchronized (worldsData) {
			if (worldsData.containsKey(multiworldManager.currentWorldKey)) {
				data = worldsData.get(multiworldManager.currentWorldKey);
				if (data != null) {
					return data;
				}
				data = new WorldData(multiworldManager.currentWorld);
				worldsData.replace(multiworldManager.currentWorldKey, data);

				return data;
			}
			data = new WorldData(multiworldManager.currentWorld);
			worldsData.put(multiworldManager.currentWorldKey, data);
		}
		return data;
	}

	public void onChunkLoad(World world, WorldChunk worldChunk) {
		if (world == null || worldChunk == null || worldChunk.isEmpty()) return;
		IMap map = DataUtil.getCurrentlyShownMap();
		WorldData mapData = getData();
		if (mapData == null) return;
		ChunkData mapChunk = mapData.getChunk(worldChunk.getPos());
		ChunkUpdateEvent updateEvent = new ChunkUpdateEvent(worldChunk, mapChunk, map.getLayer(), map.getLevel(), 0, 0, 16, 16, true);
		ChunkUpdateListener.accept(updateEvent);
	}

	public void updateOnTick() {
		if (!JustMapClient.canMapping()) return;
		getData().updateMap();
		JustMap.WORKER.execute(() -> {
			synchronized (worldsData) {
				worldsData.forEach((id, data) -> {
					if (data != null) data.clearCache();
				});
			}
		});
	}

	public void memoryControl() {
		if (cacheClearing) return;
		long usedPct = MemoryUtil.getMemoryUsage();
		if (usedPct >= 85L) {
			cacheClearing = true;
			JustMap.LOGGER.debug(String.format("Memory usage at %2d%%, forcing garbage collection.", usedPct));
			JustMap.WORKER.execute("Hard cache clearing...", () -> {
				int amount = ClientSettings.purgeAmount * 10;
				synchronized (worldsData) {
					worldsData.forEach((id, world) -> {
						if (world != null) {
							world.getChunkManager().purge(amount, 1000);
						}
					});
				}
				System.gc();
				cacheClearing = false;
			});
			usedPct = MemoryUtil.getMemoryUsage();
			JustMap.LOGGER.debug(String.format("Memory usage at %2d%%.", usedPct));
		}
	}

	public void onServerConnect() {
		multiworldManager.onServerConnect();
	}

	private void clearData() {
		synchronized (worldsData) {
			if (worldsData.size() > 0) {
				worldsData.forEach((id, data) -> {
					if (data != null) data.close();
				});
				worldsData.clear();
			}
		}
	}

	@Override
	public void onSetBlockState(BlockPos pos, BlockState state, World world) {
		ChunkUpdateListener.onSetBlockState(pos, state, world);
	}

	@Override
	public void onTick(boolean isServer) {
		multiworldManager.onTick(isServer);
		if (!isServer) {
			updateOnTick();
			memoryControl();
		}
		ChunkUpdateListener.proceed();
	}

	@Override
	public void onWorldStop() {
		multiworldManager.onWorldStop(this::clearData);
		ChunkUpdateListener.stop();
	}

	@Override
	public int getMapHeight(Layer mapLayer, int mapLevel, int posX, int posZ) {
		int chunkX = posX >> 4;
		int chunkZ = posZ >> 4;

		ChunkData mapChunk = this.getData().getChunk(chunkX, chunkZ);

		int cx = posX - (chunkX << 4);
		int cz = posZ - (chunkZ << 4);

		return mapChunk.getChunkLevel(mapLayer, mapLevel).sampleHeightmap(cx, cz);
	}
}
