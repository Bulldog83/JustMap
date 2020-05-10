package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.MapTexture;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.StorageUtil;
import ru.bulldog.justmap.util.TaskManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MapCache {
	private final static MinecraftClient minecraft = MinecraftClient.getInstance();
	private final static TaskManager mapUpdater = TaskManager.getManager("cache-data");
	
	private static Map<Integer, MapCache> dimensions = new HashMap<>();
	private static World currentWorld;
	private static Layer.Type currentLayer = Layer.Type.SURFACE;	
	private static int currentLevel = 0;
	private static int currentDimension = 0;
	
	public static boolean renewNeeded = false;
	
	public static void setCurrentLayer(Layer.Type layer, int y) {
		currentLevel =  y / layer.value.height;
		currentLayer = layer;
	}
	
	public static void setLayerLevel(int level) {
		currentLevel = level > 0 ? level : 0;
	}
	
	public static MapCache get() {
		if (currentWorld == null || (minecraft.world != null &&
									 minecraft.world != currentWorld)) {
			
			currentWorld = minecraft.world;
		}
		
		if (currentWorld == null) return null;
		
		int dimId = currentWorld.dimension.getType().getRawId();
		if(currentDimension != dimId) {
			StorageUtil.updateCacheStorage();
			currentDimension = dimId;
		}
		
		return get(currentWorld, currentDimension);
	}
	
	public static MapCache get(World world, int dimension) {	
		MapCache data = getData(world, dimension);
		
		if (data == null) return null;
		
		if (data.world != world) {
			data.world = world;
			data.clear();
			
			JustMapClient.MAP.getImage().clear();
		}
		
		return data;
	}
	
	private static MapCache getData(World world, int dimendion) {
		if (world == null) return null;
		
		if (dimensions.containsKey(dimendion)) {
			return dimensions.get(dimendion);
		}
		
		MapCache data = new MapCache(world);
		dimensions.put(dimendion, data);
		
		return data;
	}
	
	public static void saveData() {
		MapCache data = get();
		if (data == null) return;
		
		JustMap.WORKER.execute(() -> {
			data.getChunks().forEach((pos, chunk) -> {
				storeChunk(chunk);
			});
		});
	}
	
	private static void storeChunk(MapChunk chunk) {
		if (chunk.saveNeeded()) {
			CompoundTag chunkData = new CompoundTag();
			chunk.store(chunkData);
			
			if (!chunkData.isEmpty()) {
				chunkData.putInt("version", 2);
				StorageUtil.saveCache(chunk.getPos(), chunkData);
			}
		}
	}
	
	public World world;
	
	private ConcurrentMap<ChunkPos, MapChunk> chunks;
	
	private int updateIndex = 0;
	private int updatePerCycle = 10;
	private long lastPurged = 0;
	private long purgeDelay = 1000;
	private int purgeAmount = 500;
	
	private MapCache(World world) {
		this.world = world;		
		this.chunks = new ConcurrentHashMap<>();
	}
	
	public void update(Minimap map, int size, int x, int z) {
		mapUpdater.execute(() -> {
			this.updateMap(map, size, x, z);
		});
	}
	
	public void updateMap(Minimap map, int size, int centerX, int centerZ) {
		this.updatePerCycle = ClientParams.updatePerCycle;
		this.purgeDelay = ClientParams.purgeDelay * 1000;
		this.purgeAmount = ClientParams.purgeAmount;
		
		int left = centerX - size / 2;
		int top = centerZ - size / 2;
		
		MapTexture mapImage = map.getImage();
		
		int chunks = (size >> 4) + 4;
		
		int startX = (left >> 4) - 2;
		int startZ = (top >> 4) - 2;
		int endX = startX + chunks;
		int endZ = startZ + chunks;

		int offsetX = (startX << 4) - left;
		int offsetZ = (startZ << 4) - top;
		
		int index = 0, posX = 0;		
		for (int chunkX = startX; chunkX < endX; chunkX++) {
			int posZ = 0;
			int imgX = (posX << 4) + offsetX;
			for (int chunkZ = startZ; chunkZ < endZ; chunkZ++) {
				index++;

				MapChunk mapChunk = this.getCurrentChunk(chunkX, chunkZ);
				if (index >= updateIndex && index <= updateIndex + updatePerCycle) {
					mapChunk.update();
				}
				
				int imgY = (posZ << 4) + offsetZ;
				mapImage.writeChunkData(imgX, imgY, mapChunk.getColorData());
				posZ++;
			}			
			posX++;
		}
		
		map.changed = true;

		updateIndex += updatePerCycle;
		if (updateIndex >= chunks * chunks) {
			updateIndex = 0;
		}
		
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
			if (currentTime - chunkData.requested >= 30000) {
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
		this.chunks.clear();
	}
}
