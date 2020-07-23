package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.config.ClientParams;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DimensionData {
	private World world;
	private ChunkDataManager chunkManager;
	private Map<RegionPos, RegionData> regions = new ConcurrentHashMap<>();
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	
	public DimensionData(World world) {
		this.chunkManager = new ChunkDataManager(this, world);
		this.world = world;
	}

	public ChunkDataManager getChunkManager() {
		return this.chunkManager;
	}
	
	public RegionData getRegion(BlockPos currentPos, BlockPos centerPos) {
		return this.getRegion(world, currentPos, centerPos, false);
	}
	
	public RegionData getRegion(BlockPos currentPos, BlockPos centerPos, boolean surfaceOnly) {
		return this.getRegion(world, currentPos, centerPos, surfaceOnly);
	}
	
	public RegionData getRegion(World world, BlockPos currentPos, BlockPos centerPos, boolean surfaceOnly) {
		RegionData region = this.getRegion(world, currentPos);
		region.surfaceOnly = surfaceOnly;
		ChunkPos center = new ChunkPos(centerPos);
		if (!region.getCenter().equals(center)) {
			region.setCenter(center);
		}
		
		long time = System.currentTimeMillis();
		if (time - region.updated > 1000) {
			region.updateImage(ClientParams.forceUpdate);
		}
		
		return region;
	}
	
	public RegionData getRegion(World world, BlockPos currentPos) {
		RegionPos regPos = new RegionPos(currentPos);
		
		RegionData region;
		if(regions.containsKey(regPos)) {
			region = regions.get(regPos);
			region.updateWorld(world);
		} else {
			region = new RegionData(this, world, currentPos);
			regions.put(regPos, region);
		}
		
		return region;
	}
	
	public ChunkData getChunk(ChunkPos chunkPos) {
		return this.chunkManager.getChunk(chunkPos.x, chunkPos.z);
	}
	
	public ChunkData getChunk(int x, int z) {
		return this.chunkManager.getChunk(x, z);
	}

	public void callSavedChunk(ChunkPos chunkPos) {
		this.chunkManager.callSavedChunk(world, chunkPos);
	}

	public World getWorld() {
		return this.world;
	}
	
	public void updateWorld(World world) {
		this.chunkManager.updateWorld(world);
		this.world = world;
	}
	
	public void clearCache() {
		this.purgeDelay = ClientParams.purgeDelay * 1000;
		this.purgeAmount = ClientParams.purgeAmount;
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPurged > purgeDelay) {
			this.chunkManager.purge(purgeAmount, 5000);
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
