package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapCache {
	private final static MinecraftClient minecraft = MinecraftClient.getInstance();
	
	private static Map<Identifier, MapCache> dimensions = new ConcurrentHashMap<>();
	private static Identifier lastDimension;
	private static World lastWorld;
	private static Layer.Type currentLayer = Layer.Type.SURFACE;
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
		if (world != null) {
			if (minecraft.isIntegratedServerRunning()) {
				World serverWorld = minecraft.getServer().getWorld(world.getRegistryKey());
				if (serverWorld != null) {
					world = serverWorld;
				}
			}
		}
		if (world == null) return null;
		if (!world.equals(lastWorld)) {
			lastWorld = world;
		}		
		Identifier dimId = lastWorld.getDimensionRegistryKey().getValue();
		if (!dimId.equals(lastDimension)) {
			lastDimension = dimId;
		}
		return get(lastWorld, lastDimension);
	}
	
	private static MapCache get(World world, Identifier dimension) {
		if (world == null) return null;
		
		if (dimensions.containsKey(dimension)) {
			MapCache data = dimensions.get(dimension);
			if (!data.world.equals(world)) {
				data.chunks.clear();
				data.world = world;
			}
			data.clearCache();
			
			return data;
		}
		
		MapCache data = new MapCache(world);
		dimensions.put(dimension, data);
		
		return data;
	}
	
	public static void addLoadedChunk(World world, WorldChunk lifeChunk) {
		if (world == null || lifeChunk == null || lifeChunk.isEmpty()) return;
		
		MapCache data = get();
		ChunkPos chunkPos = lifeChunk.getPos();
		if (data.chunks.containsKey(chunkPos)) {
			MapChunk mapChunk = data.chunks.get(chunkPos);
			mapChunk.updateWorldChunk(lifeChunk);
			mapChunk.updateWorld(world);
		} else {
			MapChunk mapChunk = new MapChunk(world, lifeChunk, currentLayer, currentLevel);
			data.chunks.put(chunkPos, mapChunk);
		}
	}
	
	public static void clearData() {
		if (dimensions.size() > 0) {
			dimensions.forEach((id, data) -> data.clear());
			dimensions.clear();
		}
	}
	
	public World world;
	
	private Map<ChunkPos, MapChunk> chunks = new ConcurrentHashMap<>();
	private Map<RegionPos, MapRegion> regions = new ConcurrentHashMap<>();
	
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	
	private MapCache(World world) {
		this.world = world;
	}
	
	private void clearCache() {
		this.purgeDelay = ClientParams.purgeDelay * 1000;
		this.purgeAmount = ClientParams.purgeAmount;
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPurged > purgeDelay) {
			JustMap.WORKER.execute("Remove unnecessary chunks...", () -> this.purge(purgeAmount));
			this.lastPurged = currentTime;
		}
	}
	
	private void purge(int maxPurged) {
		long currentTime = System.currentTimeMillis();
		int purged = 0;
	
		List<ChunkPos> chunks = new ArrayList<>();
		for (ChunkPos chunkPos : this.chunks.keySet()) {
			MapChunk chunkData = this.chunks.get(chunkPos);
			if (currentTime - chunkData.requested >= 300000) {
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
		return this.getRegion(world, blockPos, false);
	}
	
	public MapRegion getRegion(BlockPos blockPos, boolean surfaceOnly) {
		return this.getRegion(world, blockPos, surfaceOnly);
	}
	
	public MapRegion getRegion(World world, BlockPos blockPos, boolean surfaceOnly) {
		RegionPos regPos = new RegionPos(blockPos);

		Layer.Type layer = surfaceOnly ? Layer.Type.SURFACE : currentLayer;
		int level = surfaceOnly ? 0 : currentLevel;
		
		MapRegion region;
		if(regions.containsKey(regPos)) {
			region = regions.get(regPos);
			region.updateWorld(world);
		} else {
			region = new MapRegion(world, blockPos, layer, level);
			regions.put(regPos, region);
		}
		region.surfaceOnly = surfaceOnly;
		
		long time = System.currentTimeMillis();
		if (layer != region.getLayer() ||
			level != region.getLevel()) {
			region.swapLayer(layer, level);
		} else if (time - region.updated > 3000) {
			region.updateImage(ClientParams.forceUpdate);
		}
		
		return region;
	}

	public MapChunk getCurrentChunk(int posX, int posZ) {
		return this.getChunk(currentLayer, currentLevel, posX, posZ);
	}
	
	public MapChunk getChunk(Layer.Type layer, int level, int posX, int posZ) {
		ChunkPos chunkPos = new ChunkPos(posX, posZ);

		MapChunk mapChunk;
		if (chunks.containsKey(chunkPos)) {
			mapChunk = this.chunks.get(chunkPos);
			mapChunk.updateWorld(world);
		} else {
			mapChunk = new MapChunk(world, chunkPos, layer, level);
			this.chunks.put(chunkPos, mapChunk);
		}
		
		mapChunk.setLevel(layer, level);
		mapChunk.requested = System.currentTimeMillis();
		
		return mapChunk;
	}
	
	public void clear() {
		if (regions.size() > 0) {
			regions.forEach((pos, region) -> region.close());
			regions.clear();
		}
		this.chunks.clear();
	}
}
