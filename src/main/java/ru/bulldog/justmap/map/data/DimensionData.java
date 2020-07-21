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
	private World world;
	private ChunkDataManager chunkManager;
	private Map<RegionPos, RegionData> regions = new ConcurrentHashMap<>();
	private Layer layer = Layer.SURFACE;
	private int level = 0;
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	
	public DimensionData(World world) {
		this.chunkManager = new ChunkDataManager(this, world);
		this.world = world;
	}
	
	public void setLayer(Layer layer, int level) {
		this.layer = layer;
		this.level = level;
	}
	
	public Layer getLayer() {
		return this.layer;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public RegionData getRegion(BlockPos currentPos, BlockPos centerPos) {
		return this.getRegion(world, currentPos, centerPos, false);
	}
	
	public RegionData getRegion(BlockPos currentPos, BlockPos centerPos, boolean surfaceOnly) {
		return this.getRegion(world, currentPos, centerPos, surfaceOnly);
	}
	
	public RegionData getRegion(World world, BlockPos currentPos, BlockPos centerPos, boolean surfaceOnly) {
		Layer layer = surfaceOnly ? Layer.SURFACE : this.layer;
		int level = surfaceOnly ? 0 : this.level;
		
		RegionData region = this.getRegion(world, currentPos);
		region.surfaceOnly = surfaceOnly;
		ChunkPos center = new ChunkPos(centerPos);
		if (!region.getCenter().equals(center)) {
			region.setCenter(center);
		}
		
		long time = System.currentTimeMillis();
		if (layer != region.getLayer() ||
			level != region.getLevel()) {
			region.swapLayer(layer, level);
		} else if (time - region.updated > 1000) {
			region.updateImage(ClientParams.forceUpdate);
		}
		region.updateWorld(world);
		
		return region;
	}
	
	public RegionData getRegion(World world, BlockPos currentPos) {
		RegionPos regPos = new RegionPos(currentPos);
		
		RegionData region;
		if(regions.containsKey(regPos)) {
			region = regions.get(regPos);
		} else {
			region = new RegionData(this, world, currentPos, layer, level);
			regions.put(regPos, region);
		}
		
		return region;
	}

	public void addLoadedChunk(World world, WorldChunk lifeChunk) {
		this.chunkManager.addLoadedChunk(world, lifeChunk);
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
