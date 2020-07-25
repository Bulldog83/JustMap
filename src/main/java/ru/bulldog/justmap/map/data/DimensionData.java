package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.event.ChunkUpdateEvent;
import ru.bulldog.justmap.event.ChunkUpdateListener;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.math.MathUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

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
	
	public RegionData getRegion(BlockPos currentPos, BlockPos centerPos, boolean worldmap) {
		return this.getRegion(world, currentPos, centerPos, worldmap);
	}
	
	public RegionData getRegion(World world, BlockPos currentPos, BlockPos centerPos, boolean worldmap) {
		RegionData region = this.getRegion(world, currentPos, worldmap);
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
	
	public RegionData getRegion(World world, BlockPos currentPos, boolean worldmap) {
		RegionPos regPos = new RegionPos(currentPos);
		
		RegionData region;
		if(regions.containsKey(regPos)) {
			region = regions.get(regPos);
			region.updateWorld(world);
		} else {
			region = new RegionData(this, world, currentPos, worldmap);
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
	
	public WorldChunk getWorldChunk(BlockPos blockPos) {
		int x = blockPos.getX() >> 4;
		int z = blockPos.getZ() >> 4;
		
		WorldChunk worldChunk = (WorldChunk) this.world.getChunk(x, z, ChunkStatus.FULL, false);
		return worldChunk != null ? worldChunk : this.getEmptyChunk();
	}
	
	public WorldChunk getEmptyChunk() {
		return this.chunkManager.getEmptyChunk();
	}

	public void callSavedChunk(ChunkPos chunkPos, Layer layer, int level) {
		this.chunkManager.callSavedChunk(world, chunkPos, layer, level);
	}

	public World getWorld() {
		return this.world;
	}
	
	public void updateWorld(World world) {
		this.chunkManager.updateWorld(world);
		this.world = world;
	}
	
	public void updateMap() {
		IMap map = DataUtil.getMap();
		BlockPos centerPos = map.getCenter();
		Layer layer = map.getLayer();
		int level = map.getLevel();
		boolean update = ClientParams.forceUpdate;
		
		ChunkData mapChunk = this.getChunk(new ChunkPos(centerPos));
		WorldChunk worldChunk = this.getWorldChunk(centerPos);
		if (!worldChunk.isEmpty()) {
			ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, update));
		}
		int x = centerPos.getX();
		int z = centerPos.getZ();
		int distance = DataUtil.getGameOptions().viewDistance;
		BlockPos.Mutable currentPos = centerPos.mutableCopy();
		for (int step = 1; step < distance + 1; step++) {
			boolean even = MathUtil.isEven(step);
			for (int i = 0; i < step; i++) {
				if (even) {
					currentPos.setX(x -= 16);
				} else {
					currentPos.setX(x += 16);
				}
				mapChunk = this.getChunk(new ChunkPos(currentPos));
				worldChunk = this.getWorldChunk(currentPos);
				if (!worldChunk.isEmpty()) {
					ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, update));
				}
			}
			for (int i = 0; i < step; i++) {
				if (even) {
					currentPos.setZ(z -= 16);
				} else {
					currentPos.setZ(z += 16);
				}
				mapChunk = this.getChunk(new ChunkPos(currentPos));
				worldChunk = this.world.getWorldChunk(currentPos);
				if (!worldChunk.isEmpty()) {
					ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, update));
				}
			}
		}
		
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
		this.chunkManager.clear();
	}
	
	public void close() {
		this.regions.forEach((pos, region) -> region.close());
		this.regions.clear();
		this.clear();
	}
}
