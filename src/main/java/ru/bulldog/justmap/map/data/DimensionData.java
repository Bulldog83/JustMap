package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DimensionData {
	private static Layer.Type currentLayer = Layer.Type.SURFACE;
	private static int currentLevel = 0;
	
	public static void setCurrentLayer(Layer.Type layer, int y) {
		currentLevel =  y / layer.value.height;
		currentLayer = layer;
	}
	
	public static Layer.Type currentLayer() {
		return currentLayer;
	}
	
	public static int currentLevel() {
		return currentLevel;
	}
	
	private World world;
	private ChunkDataManager chunkManager;
	private Map<RegionPos, MapRegion> regions = new ConcurrentHashMap<>();
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	
	public DimensionData(World world) {
		this.chunkManager = new ChunkDataManager(world);
		this.world = world;
	}
	
	public MapRegion getRegion(BlockPos currentPos, BlockPos centerPos) {
		return this.getRegion(world, currentPos, centerPos, false);
	}
	
	public MapRegion getRegion(BlockPos currentPos, BlockPos centerPos, boolean surfaceOnly) {
		return this.getRegion(world, currentPos, centerPos, surfaceOnly);
	}
	
	public MapRegion getRegion(World world, BlockPos currentPos, BlockPos centerPos, boolean surfaceOnly) {
		RegionPos regPos = new RegionPos(currentPos);

		Layer.Type layer = surfaceOnly ? Layer.Type.SURFACE : currentLayer;
		int level = surfaceOnly ? 0 : currentLevel;
		
		MapRegion region;
		if(regions.containsKey(regPos)) {
			region = regions.get(regPos);
		} else {
			region = new MapRegion(world, currentPos, layer, level);
			regions.put(regPos, region);
		}
		region.surfaceOnly = surfaceOnly;
		ChunkPos center = new ChunkPos(centerPos);
		if (!region.getCenter().equals(center)) {
			region.setCenter(center);
		}
		
		long time = System.currentTimeMillis();
		if (layer != region.getLayer() ||
			level != region.getLevel()) {
			region.swapLayer(layer, level);
		} else if (time - region.updated > 3000) {
			region.updateImage(ClientParams.forceUpdate);
		}
		region.updateWorld(world);
		
		return region;
	}

	public void addLoadedChunk(World world, WorldChunk lifeChunk) {
		this.chunkManager.addLoadedChunk(world, lifeChunk);
	}
	
	public ChunkData getCurrentChunk(int x, int z) {
		return this.chunkManager.getChunk(currentLayer, currentLevel, x, z);
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public void updateWorld(World world) {
		this.world = world;
	}
	
	public ChunkDataManager getChunkManager() {
		return this.chunkManager;
	}
	
	public void clearCache() {
		this.purgeDelay = ClientParams.purgeDelay * 1000;
		this.purgeAmount = ClientParams.purgeAmount;
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPurged > purgeDelay) {
			JustMap.WORKER.execute("Remove unnecessary chunks...", () -> this.chunkManager.purge(purgeAmount, 60000));
			this.lastPurged = currentTime;
		}
	}
	
	public void clear() {
		if (regions.size() > 0) {
			regions.forEach((pos, region) -> region.close());
			regions.clear();
		}
		this.chunkManager.clear();
	}
}
