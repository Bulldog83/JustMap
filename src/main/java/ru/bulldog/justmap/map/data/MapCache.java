package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.StorageUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapCache {
	private final static MinecraftClient minecraft = MinecraftClient.getInstance();
	
	private static Map<Identifier, MapCache> dimensions = new HashMap<>();
	private static World currentWorld;
	private static Layer.Type currentLayer = Layer.Type.SURFACE;
	private static Identifier currentDimension = DimensionType.OVERWORLD_REGISTRY_KEY.getValue();
	private static int currentLevel = 0;
	public static long lastSaved = 0;
	
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
	
	public static void setLayerLevel(int level) {
		currentLevel = level > 0 ? level : 0;
	}
	
	public static MapCache get() {
		World world = minecraft.world;
		if (minecraft.isIntegratedServerRunning() && world != null) {
			world = minecraft.getServer().getWorld(minecraft.world.getRegistryKey());
		}
		if (currentWorld == null || (world != null &&
									 world != currentWorld)) {
			
			currentWorld = world;
		}
		
		if (currentWorld == null) return null;
		
		Identifier dimId = currentWorld.getDimensionRegistryKey().getValue();
		if(currentDimension != dimId) {
			StorageUtil.updateCacheStorage();
			currentDimension = dimId;
		}
		
		return get(currentWorld, currentDimension);
	}
	
	public static MapCache get(World world, Identifier dimension) {	
		MapCache data = getData(world, dimension);
		
		if (data == null) return null;
		
		if (data.world != world) {
			data.world = world;
			data.clear();
		} else {		
			data.clearCache();
		}
		
		return data;
	}
	
	private static MapCache getData(World world, Identifier dimension) {
		if (world == null) return null;
		
		if (dimensions.containsKey(dimension)) {
			return dimensions.get(dimension);
		}
		
		MapCache data = new MapCache(world);
		dimensions.put(dimension, data);
		
		return data;
	}
	
	public static DimensionType getDimension() {
		return DimensionType.byRawId(currentDimension);
	}
	
	public static void saveData() {
		MapCache data = get();
		if (data == null) return;
		
		JustMap.WORKER.execute(() -> {
			data.getChunks().forEach((pos, chunk) -> {
				storeChunk(chunk);
			});
		});
		lastSaved = System.currentTimeMillis();
	}
	
	public static void storeChunk(MapChunk chunk) {
		if (!chunk.saving && chunk.saveNeeded()) {
			chunk.saving = true;
			
			CompoundTag chunkData = new CompoundTag();
			chunk.store(chunkData);
			
			if (!chunkData.isEmpty()) {
				chunkData.putInt("version", 2);
				StorageUtil.saveCache(chunk.getPos(), chunkData);
			}
			chunk.saving = false;
		}
	}
	
	public World world;
	
	private Map<ChunkPos, MapChunk> chunks;
	private Map<RegionPos, MapRegion> regions;
	
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	
	private MapCache(World world) {
		this.world = world;		
		this.chunks = new ConcurrentHashMap<>();
		this.regions = new ConcurrentHashMap<>();
	}
	
	private void clearCache() {
		this.purgeDelay = ClientParams.purgeDelay * 1000;
		this.purgeAmount = ClientParams.purgeAmount;
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPurged > purgeDelay) {
			JustMap.WORKER.execute(() -> {
				this.purge(purgeAmount);
			});
			this.lastPurged = currentTime;
		}
	}
	
	private void purge(int maxPurged) {
		long currentTime = System.currentTimeMillis();
		int purged = 0;
	
		List<ChunkPos> chunks = new ArrayList<>();
		for (ChunkPos chunkPos : this.chunks.keySet()) {
			MapChunk chunkData = this.chunks.get(chunkPos);
			if (currentTime - chunkData.requested >= 5000) {
				storeChunk(chunkData);
				chunks.add(chunkPos);
				purged++;
				if (purged >= maxPurged) {
					break;
				}
			}
		}
	
		for (ChunkPos chunkPos : chunks) {
			this.chunks.remove(chunkPos);
		}
	}
	
	public MapRegion getRegion(BlockPos blockPos) {
		return this.getRegion(blockPos, false);
	}
	
	public MapRegion getRegion(BlockPos blockPos, boolean surfaceOnly) {
		RegionPos regPos = new RegionPos(blockPos);

		Layer.Type layer = surfaceOnly ? Layer.Type.SURFACE : currentLayer;
		int level = surfaceOnly ? 0 : currentLevel;
		
		MapRegion region;
		if(regions.containsKey(regPos)) {
			region = this.regions.get(regPos);
		} else {
			region = new MapRegion(blockPos, layer, level);
			this.regions.put(regPos, region);
		}
		region.surfaceOnly = surfaceOnly;
		
		long time = System.currentTimeMillis();
		if (layer != region.getLayer() ||
			level != region.getLevel()) {
			region.swapLayer(layer, level);
		} else if (time - region.updated > 1000) {
			region.updateTexture();
		}
		
		return region;
	}
	
	private Map<ChunkPos, MapChunk> getChunks() {
		return this.chunks;
	}
	
	public MapChunk getCurrentChunk(ChunkPos chunkPos) {
		return this.getChunk(currentLayer, currentLevel, chunkPos.x, chunkPos.z);
	}
	
	public MapChunk getCurrentChunk(int posX, int posZ) {
		return this.getChunk(currentLayer, currentLevel, posX, posZ);
	}
	
	public MapChunk getChunk(Layer.Type layer, int level, int posX, int posZ) {
		ChunkPos chunkPos = new ChunkPos(posX, posZ);		
		
		MapChunk mapChunk;
		if (chunks.containsKey(chunkPos)) {
			mapChunk = this.chunks.get(chunkPos);
		} else {
			mapChunk = new MapChunk(world, chunkPos, layer, level);
			this.chunks.put(chunkPos, mapChunk);
		}
		
		mapChunk.setLevel(layer, level);
		mapChunk.requested = System.currentTimeMillis();
		
		return mapChunk;
	}
	
	private void clear() {
		this.regions.forEach((pos, region) -> {
			region.close();
		});
		this.regions.clear();
		this.chunks.clear();
	}
}
