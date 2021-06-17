package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.event.ChunkUpdateEvent;
import ru.bulldog.justmap.event.ChunkUpdateListener;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.math.MathUtil;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class WorldData {
	private Level world;
	private final ChunkDataManager chunkManager;
	private final Map<RegionPos, RegionData> regions;
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	
	public WorldData(Level world) {
		this.regions = new HashMap<>();
		this.chunkManager = new ChunkDataManager(this, world);
		this.world = world;
	}

	public ChunkDataManager getChunkManager() {
		return this.chunkManager;
	}
	
	public RegionData getRegion(BlockPos blockPos) {
		return this.getRegion(DataUtil.getMap(), blockPos);
	}
	
	public RegionData getRegion(IMap map, BlockPos blockPos) {
		RegionPos regPos = new RegionPos(blockPos);
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
		region.setIsWorldmap(map.isWorldmap());
		long time = System.currentTimeMillis();
		if (time - region.updated > 1000) {
			region.updateImage(ClientSettings.forceUpdate);
		}
		
		return region;
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
	
	public LevelChunk getWorldChunk(BlockPos blockPos) {
		return this.getWorldChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
	}
	
	public LevelChunk getWorldChunk(int x, int z) {
		LevelChunk worldChunk = this.world.getChunk(x, z);
		if (worldChunk.isEmpty()) {
			worldChunk = this.callSavedChunk(worldChunk.getPos());
		}
		return worldChunk;
	}
	
	public LevelChunk getEmptyChunk() {
		return this.chunkManager.getEmptyChunk();
	}

	public LevelChunk callSavedChunk(ChunkPos chunkPos) {
		Level world = DataUtil.getWorld();
		return this.chunkManager.callSavedChunk(world, chunkPos);
	}

	public Level getWorld() {
		return this.world;
	}
	
	public void updateMap() {
		IMap map = DataUtil.getMap();
		BlockPos centerPos = map.getCenter();
		Layer layer = map.getLayer();
		int level = map.getLevel();
		boolean update = ClientSettings.forceUpdate;
		
		long time = System.currentTimeMillis();
		long interval = ClientSettings.chunkUpdateInterval;
		ChunkData mapChunk = this.getChunk(centerPos);
		LevelChunk worldChunk = this.world.getChunkAt(centerPos);
		boolean chunkLoaded = !worldChunk.isEmpty() && mapChunk.isChunkLoaded();
		if (chunkLoaded && time - mapChunk.updated > interval) {
			ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, 0, 0, 16, 16, update));
		}
		int x = centerPos.getX();
		int z = centerPos.getZ();
		int distance = DataUtil.getGameOptions().renderDistance - 1;
		BlockPos.MutableBlockPos currentPos = centerPos.mutable();
		for (int step = 1; step < distance * 2; step++) {
			boolean even = MathUtil.isEven(step);
			for (int i = 0; i < step; i++) {
				if (even) {
					currentPos.setX(x -= 16);
				} else {
					currentPos.setX(x += 16);
				}
				mapChunk = this.getChunk(currentPos);
				worldChunk = this.world.getChunkAt(currentPos);
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
				worldChunk = this.world.getChunkAt(currentPos);
				chunkLoaded = !worldChunk.isEmpty() && mapChunk.isChunkLoaded();
				if (chunkLoaded && time - mapChunk.updated > interval) {
					ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, 0, 0, 16, 16, update));
				}
			}
		}
		
	}
	
	public void clearCache() {
		this.purgeDelay = ClientSettings.purgeDelay * 1000;
		this.purgeAmount = ClientSettings.purgeAmount;
		
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
		synchronized (regions) {
			this.regions.forEach((pos, region) -> region.close());
			this.regions.clear();
		}
		this.clear();
	}
}
