package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapCache {
	private final static MinecraftClient minecraft = MinecraftClient.getInstance();
	
	private static Map<Integer, MapCache> dimensions = new HashMap<>();
	private static Layer currentLayer = Layer.SURFACE;	
	private static int currentLevel = 0;
	
	public static void setCurrentLayer(Layer layer, int y) {
		currentLevel =  y / layer.height;
		currentLayer = layer;
	}
	
	public static void setLayerLevel(int level) {
		currentLevel = level > 0 ? level : 0;
	}
	
	public static MapCache get() {
		return get(minecraft.world);
	}
	
	public static MapCache get(World world) {
		MapCache data = getDimensionData(world);
		
		if (data == null) return null;
		
		if (data.world != world) {
			data.world = world;
			data.clear();
			
			ImageUtil.fillImage(JustMapClient.MAP.getImage(), Colors.BLACK);		   
		}
		
		return data;
	}
	
	private static MapCache getDimensionData(World world) {		
		if (world == null) return null;
		
		int dimId = world.dimension.getType().getRawId();
		if (dimensions.containsKey(dimId)) {
			return dimensions.get(dimId);
		}
		
		MapCache data = new MapCache(world);
		dimensions.put(dimId, data);
		
		return data;
	}
	
	public World world;
	
	private Map<ChunkPos, MapChunk> chunks;
	private Map<RegionPos, MapRegion> regions;
	
	private int updateIndex = 0;
	private int updatePerCycle = 10;
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	
	private MapCache(World world) {
		this.world = world;
		
		chunks = new ConcurrentHashMap<>();
		regions = new ConcurrentHashMap<>();
	}
	
	public void update(Minimap map, int size, int x, int z) {
		updatePerCycle = ClientParams.updatePerCycle;
		purgeDelay = ClientParams.purgeDelay * 1000;
		purgeAmount = ClientParams.purgeAmount;
		
		int chunks = (size >> 4) + 4;
		int startX = (x >> 4) - 2;
		int startZ = (z >> 4) - 2;
		int endX = startX + chunks;
		int endZ = startZ + chunks;

		int offsetX = (startX << 4) - x;
		int offsetZ = (startZ << 4) - z;
		
		int index = 0, posX = 0;
		for (int chunkX = startX; chunkX < endX; chunkX++) {
			int posY = 0;
			int imgX = (posX << 4) + offsetX;
			for (int chunkZ = startZ; chunkZ < endZ; chunkZ++) {
				index++;

				MapChunk mapChunk = getChunk(chunkX, chunkZ);				
				if (index >= updateIndex && index <= updateIndex + updatePerCycle) {
					if (mapChunk.getWorldChunk().isEmpty()) {
						WorldChunk chunk = world.getChunk(chunkX, chunkZ);
						if (!chunk.isEmpty()) {
							mapChunk.setChunk(chunk);
						}
					}
					if (!mapChunk.getWorldChunk().isEmpty() || mapChunk.isEmpty()) {
						updateChunk(mapChunk);
					}
				}
				
				int imgY = (posY << 4) + offsetZ;
				ImageUtil.writeTile(map.getImage(), mapChunk.getImage(), imgX, imgY);
				
				posY++;
			}
			
			posX++;
		}
		
		updateIndex += updatePerCycle;
		if (updateIndex >= chunks * chunks) {
			updateIndex = 0;
		}
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastPurged > purgeDelay) {
			JustMap.EXECUTOR.execute(() -> {
				purge(purgeAmount);
			});
			lastPurged = currentTime;
		}
	}
	
	private void updateChunk(MapChunk mapChunk) {
		JustMapClient.UPDATER.execute(() -> {
			mapChunk.update();
		});
	}
	
	private void purge(int maxPurged) {
		long currentTime = System.currentTimeMillis();
		int purged = 0;
	
		List<ChunkPos> chunks = new ArrayList<>();
		for (ChunkPos chunkPos : getChunks().keySet()) {
			MapChunk chunkData = getChunks().get(chunkPos);
			if (currentTime - chunkData.updated >= 30000) {
				chunks.add(chunkPos);
				purged++;
				if (purged >= maxPurged) {
					break;
				}
			}
		}
	
		for (ChunkPos chunkPos : chunks) {
			getChunks().remove(chunkPos);
		}
		
		maxPurged = maxPurged >> 5;
		
		List<RegionPos> regions = new ArrayList<>();
		for (RegionPos regionPos : getRegions().keySet()) {
			MapRegion region = getRegions().get(regionPos);
			if (currentTime - region.updated >= 30000) {
				regions.add(regionPos);
				purged++;
				if (purged >= maxPurged) {
					break;
				}
			}
		}
	
		for (RegionPos regionPos : regions) {
			getRegions().remove(regionPos);
		}
	}
	
	private synchronized Map<ChunkPos, MapChunk> getChunks() {
		return this.chunks;
	}
	
	public synchronized Map<RegionPos, MapRegion> getRegions() {
		return this.regions;
	}
	
	public MapRegion getRegion(ChunkPos chunkPos) {
		RegionPos pos = new RegionPos(chunkPos);
		
		MapRegion region;
		if (getRegions().containsKey(pos)) {
			region = getRegions().get(pos);
		} else {
			region = new MapRegion(chunkPos);
			getRegions().put(pos, region);
		}
		
		region.setLayer(currentLayer);
		region.setLevel(currentLevel);
		
		return region;
	}
	
	public synchronized MapChunk getChunk(int posX, int posZ) {
		return getChunk(posX, posZ, false);
	}
	
	public synchronized MapChunk getChunk(int posX, int posZ, boolean empty) {
		return getChunk(currentLayer, currentLevel, posX, posZ, empty);
	}
	
	public synchronized MapChunk getChunk(Layer layer, int level, int posX, int posZ, boolean empty) {
		
		MapChunk mapChunk = getChunk(layer, level, posX, posZ);
		
		ChunkPos chunkPos = new ChunkPos(posX, posZ);
		if(!mapChunk.getWorldChunk().getPos().equals(chunkPos)) {
			mapChunk = new MapChunk(world, chunkPos, layer, level);
			getChunks().put(chunkPos, mapChunk);
		}
		
		mapChunk.setLevel(layer, level);
		mapChunk.setEmpty(empty);
		
		return mapChunk;
	}
	
	public synchronized MapChunk getChunk(Layer layer, int level, int posX, int posZ) {
		ChunkPos chunkPos = new ChunkPos(posX, posZ);
		if (getChunks().containsKey(chunkPos)) {
			return getChunks().get(chunkPos);
		}
		
		MapChunk mapChunk = new MapChunk(world, chunkPos, layer, level);
		getChunks().put(chunkPos, mapChunk);
		
		return mapChunk;
	}
	
	private void clear() {
		getChunks().clear();
		getRegions().clear();
	}
}
