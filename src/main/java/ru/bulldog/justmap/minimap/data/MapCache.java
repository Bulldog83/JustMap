package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapCache {
	private static MapProcessor.Layer currentLayer = MapProcessor.Layer.SURFACE;
	private static Map<Integer, Map<MapProcessor.Layer, MapCache>> dimensions = new HashMap<>();
	
	public static void setCurrentLayer(MapProcessor.Layer layer) {
		currentLayer = layer;
	}
	
	public static MapProcessor.Layer getCurrentLayer() {
		return currentLayer;
	}
	
	public static MapCache get(World world) {
		return get(world, currentLayer);
	}
	
	public static MapCache get(World world, MapProcessor.Layer layer) {
		int dimId = world.dimension.getType().getRawId();
		Map<MapProcessor.Layer, MapCache> layers = getDimensionLayers(dimId);
		
		if (layers.containsKey(layer)) {
			MapCache cache = layers.get(layer);
			if (cache.world != world) {
				cache.world = world;
				cache.clear();
				NativeImage img = JustMapClient.MAP.getImage();
				img.fillRect(0, 0, img.getWidth(), img.getHeight(), Colors.BLACK);
				
				JustMap.LOGGER.logInfo("Updated world " + world + " " + dimId);			   
			}
			
			return cache;
		}
		
		MapCache loader = new MapCache(layer, world);
		layers.put(layer, loader);
		
		return loader;
	}
	
	private void clear() {
		mapChunks.clear();
	}
	
	private static Map<MapProcessor.Layer, MapCache> getDimensionLayers(int dimId) {
		if (dimensions.containsKey(dimId)) {
			return dimensions.get(dimId);
		}
		
		HashMap<MapProcessor.Layer, MapCache> layers = new HashMap<>();
		dimensions.put(dimId, layers);
		
		return layers;
	}
	
	public final MapProcessor.Layer layer;
	public World world;
	
	private Map<ChunkPos, MapChunk> mapChunks = new HashMap<>();
	
	private int updateIndex = 0;
	private int updatePerCycle = 10;
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	
	private MapCache(MapProcessor.Layer layer, World world) {
		this.layer = layer;
		this.world = world;
	}
	
	public void update(Minimap map, int size, int x, int z) {
		updatePerCycle = ClientParams.updatePerCycle;
		purgeDelay = ClientParams.purgeDelay * 1000;
		purgeAmount = ClientParams.purgeAmount;
		
		int chunks = size / 16 + 4;
		int startX = x / 16 - 2;
		int startZ = z / 16 - 2;
		int endX = startX + chunks;
		int endZ = startZ + chunks;

		int offsetX = startX * 16 - x;
		int offsetZ = startZ * 16 - z;
		
		int index = 0, posX = 0;
		for (int chunkX = startX; chunkX < endX; chunkX++) {
			int posY = 0;
			int imgX = posX * 16 + offsetX;
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
						mapChunk.update();
					}
				}
				
				int imgY = posY * 16 + offsetZ;
				ImageUtil.writeIntoImage(mapChunk.getImage(), map.getImage(), imgX, imgY);
				
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
			purge(purgeAmount);
			lastPurged = currentTime;
		}
	}
	
	private void purge(int maxPurged) {
		long currentTime = System.currentTimeMillis();
		int purged = 0;
	
		List<ChunkPos> forPurge = new ArrayList<>();
		for (ChunkPos chunkPos : mapChunks.keySet()) {
			MapChunk chunkData = mapChunks.get(chunkPos);
			if (currentTime - chunkData.updated >= 10000) {
				forPurge.add(chunkPos);
				purged++;
				if (purged >= maxPurged) {
					break;
				}
			}
		}
	
		for (ChunkPos chunkPos : forPurge) {
			mapChunks.remove(chunkPos);
		}
	}
	
	public MapChunk getChunk(int posX, int posZ) {
		return getChunk(posX, posZ, false);
	}
	
	public MapChunk getChunk(int posX, int posZ, boolean empty) {
		ChunkPos chunkPos = new ChunkPos(posX, posZ);
		if (mapChunks.containsKey(chunkPos)) {
			MapChunk mapChunk = mapChunks.get(chunkPos);
			if (!mapChunk.getWorldChunk().getPos().equals(chunkPos)) {
				mapChunk.updateChunk();
				if (!empty) {
					mapChunk.update();
				}
			}
			
			return mapChunk;
		}
		
		int dimId = this.world.dimension.getType().getRawId();
		MapChunk mapChunk = new MapChunk(world.getChunk(posX, posZ), currentLayer);
		mapChunk.dimension = dimId;
		mapChunk.setEmpty(empty);
		
		if(!mapChunk.getPos().equals(chunkPos)) {
			mapChunk.setPos(chunkPos);
		}		
		if (!empty) {
			mapChunk.update();
		}
		
		mapChunks.put(chunkPos, mapChunk);
		
		return mapChunk;
	}	
}
