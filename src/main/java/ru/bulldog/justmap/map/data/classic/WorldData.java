package ru.bulldog.justmap.map.data.classic;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.client.screen.WorldmapScreen;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.data.MapRegionProvider;
import ru.bulldog.justmap.map.data.RegionPos;
import ru.bulldog.justmap.map.data.classic.event.ChunkUpdateEvent;
import ru.bulldog.justmap.map.data.classic.event.ChunkUpdateListener;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.math.MathUtil;

public class WorldData implements MapRegionProvider {
	private final World world;
	private final ChunkDataManager chunkManager;
	private final Map<RegionPos, RegionData> regions;
	private long lastPurged = 0;

	public WorldData(World world) {
		this.regions = new HashMap<>();
		this.chunkManager = new ChunkDataManager(this, world);
		this.world = world;
	}

	public ChunkDataManager getChunkManager() {
		return this.chunkManager;
	}

	public RegionData getRegion(BlockPos blockPos) {
		return this.getRegionData(DataUtil.getCurrentlyShownMap(), blockPos.getX(), blockPos.getZ());
	}

	public RegionData getRegionData(IMap map, int x, int z) {
		RegionPos regPos = new RegionPos(x, z);
		RegionData region;
		synchronized (regions) {
			if(regions.containsKey(regPos)) {
				region = this.regions.get(regPos);
				region.setCenter(new ChunkPos(map.getCenter()));
			} else {
				region = new RegionData(map, this, regPos);
				regions.put(regPos, region);
			}
		}
		region.setIsWorldmap(map instanceof WorldmapScreen);
		long time = System.currentTimeMillis();
		if (time - region.updated > 1000) {
			region.updateImage(ClientSettings.forceUpdate);
		}

		return region;
	}

	@Override
	public MapRegion getMapRegion(IMap map, int x, int z) {
		return getRegionData(map, x, z);
	}

	public ChunkData getChunk(BlockPos pos) {
		return this.chunkManager.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
	}

	public ChunkData getChunk(ChunkPos chunkPos) {
		return this.chunkManager.getChunk(chunkPos.x, chunkPos.z);
	}

	public ChunkData getChunk(int x, int z) {
		return this.chunkManager.getChunk(x, z);
	}

	public WorldChunk getWorldChunk(BlockPos blockPos) {
		return this.getWorldChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}

	public WorldChunk getWorldChunk(int x, int z) {
		WorldChunk worldChunk = this.world.getChunk(x, z);
		if (worldChunk.isEmpty()) {
			worldChunk = this.callSavedChunk(worldChunk.getPos());
		}
		return worldChunk;
	}

	public WorldChunk getEmptyChunk() {
		return this.chunkManager.getEmptyChunk();
	}

	public WorldChunk callSavedChunk(ChunkPos chunkPos) {
		World world = DataUtil.getWorld();
		return this.chunkManager.callSavedChunk(world, chunkPos);
	}

	public World getWorld() {
		return this.world;
	}

	public void updateMap() {
		IMap map = DataUtil.getCurrentlyShownMap();
		BlockPos centerPos = map.getCenter();
		Layer layer = map.getLayer();
		int level = map.getLevel();
		boolean update = ClientSettings.forceUpdate;

		long time = System.currentTimeMillis();
		long interval = ClientSettings.chunkUpdateInterval;
		ChunkData mapChunk = this.getChunk(centerPos);
		WorldChunk worldChunk = this.world.getWorldChunk(centerPos);
		boolean chunkLoaded = !worldChunk.isEmpty() && mapChunk.isChunkLoaded();
		if (chunkLoaded && time - mapChunk.updated > interval) {
			ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, 0, 0, 16, 16, update));
		}
		int x = centerPos.getX();
		int z = centerPos.getZ();
		int distance = DataUtil.getGameOptions().viewDistance - 1;
		BlockPos.Mutable currentPos = centerPos.mutableCopy();
		for (int step = 1; step < distance * 2; step++) {
			boolean even = MathUtil.isEven(step);
			for (int i = 0; i < step; i++) {
				if (even) {
					currentPos.setX(x -= 16);
				} else {
					currentPos.setX(x += 16);
				}
				mapChunk = this.getChunk(currentPos);
				worldChunk = this.world.getWorldChunk(currentPos);
				chunkLoaded = !worldChunk.isEmpty() && mapChunk.isChunkLoaded();
				if (chunkLoaded && time - mapChunk.updated > interval) {
					ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, 0, 0, 16, 16, update));
				}
			}
			for (int i = 0; i < step; i++) {
				if (even) {
					currentPos.setZ(z -= 16);
				} else {
					currentPos.setZ(z += 16);
				}
				mapChunk = this.getChunk(currentPos);
				worldChunk = this.world.getWorldChunk(currentPos);
				chunkLoaded = !worldChunk.isEmpty() && mapChunk.isChunkLoaded();
				if (chunkLoaded && time - mapChunk.updated > interval) {
					ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, 0, 0, 16, 16, update));
				}
			}
		}

	}

	public void clearCache() {
		long purgeDelay = ClientSettings.purgeDelay * 1000;
		int purgeAmount = ClientSettings.purgeAmount;

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPurged > purgeDelay) {
			this.chunkManager.purge(purgeAmount, 5000);
			this.lastPurged = currentTime;
		}
	}

	public void clear() {
		this.chunkManager.clear();
	}

	@Override
	public void onMultiworldClose() {
		synchronized (regions) {
			this.regions.forEach((pos, region) -> region.close());
			this.regions.clear();
		}
		this.clear();
	}
}
