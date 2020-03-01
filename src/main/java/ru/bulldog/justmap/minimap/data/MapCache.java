package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.minimap.data.MapProcessor.Layer;
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
	private static Map<Integer, MapCache> dimensions = new HashMap<>();
	
	private static int currentLevel = 0;
	
	public static void setCurrentLayer(MapProcessor.Layer layer) {
		currentLayer = layer;
	}
	
	public static MapProcessor.Layer getCurrentLayer() {
		return currentLayer;
	}
	
	public static void setLayerLevel(int level) {
		currentLevel = level;
	}
	
	public static MapCache get(World world) {
		return get(world, currentLayer);
	}
	
	public static MapCache get(World world, MapProcessor.Layer layer) {
		MapCache data = getDimensionData(world);
		if (data.world != world) {
			data.world = world;
			data.clear();
			
			NativeImage img = JustMapClient.MAP.getImage();
			img.fillRect(0, 0, img.getWidth(), img.getHeight(), Colors.BLACK);		   
		}
		
		return data;
	}
	
	private void clear() {
		mapChunks.clear();
	}
	
	private static MapCache getDimensionData(World world) {
		int dimId = world.dimension.getType().getRawId();
		if (dimensions.containsKey(dimId)) {
			return dimensions.get(dimId);
		}
		
		MapCache data = new MapCache(world);
		dimensions.put(dimId, data);
		
		return data;
	}
	
	public World world;
	
	private Map<ChunkPos, MapChunk> mapChunks = new HashMap<>();
	
	private int updateIndex = 0;
	private int updatePerCycle = 10;
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	private int levelSize;
	
	private MapCache(World world) {
		this.world = world;
		this.levelSize = ClientParams.chunkLevelSize;
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
						mapChunk.update();
					}
				}
				
				int imgY = (posY << 4) + offsetZ;
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
	
	public synchronized MapChunk getChunk(int posX, int posZ) {
		return getChunk(posX, posZ, false);
	}
	
	public synchronized MapChunk getChunk(int posX, int posZ, boolean empty) {
		return getChunk(currentLayer, currentLevel, posX, posZ, empty);
	}
	
	public synchronized MapChunk getChunk(Layer layer, int level, int posX, int posZ, boolean empty) {
		
		MapChunk mapChunk = getChunk(layer, level, posX, posZ);
		
		if (levelSize != ClientParams.chunkLevelSize) {
			levelSize = ClientParams.chunkLevelSize;
			mapChunk.resetChunk();
		}		
		
		mapChunk.setLevel(layer, level);
		mapChunk.setEmpty(empty);
		
		ChunkPos chunkPos = new ChunkPos(posX, posZ);
		if(!mapChunk.getPos().equals(chunkPos)) {
			mapChunk.setPos(chunkPos);
		}
		if (!mapChunk.getWorldChunk().getPos().equals(chunkPos)) {
			mapChunk.updateWorldChunk();
			if (!empty) {
				mapChunk.update();
			}
		}
		
		return mapChunk;
	}
	
	public synchronized MapChunk getChunk(Layer layer, int level, int posX, int posZ) {
		ChunkPos chunkPos = new ChunkPos(posX, posZ);
		if (mapChunks.containsKey(chunkPos)) {
			return mapChunks.get(chunkPos);
		}
		
		MapChunk mapChunk = new MapChunk(world.getChunk(posX, posZ), layer, level);
		mapChunks.put(chunkPos, mapChunk);
		
		return mapChunk;
	}
}
