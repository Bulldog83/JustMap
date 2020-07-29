package ru.bulldog.justmap.map.data;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.event.ChunkUpdateEvent;
import ru.bulldog.justmap.event.ChunkUpdateListener;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.tasks.MemoryUtil;

public final class DimensionManager {

	private final static Map<WorldKey, WorldData> WORLDS_DATA = new HashMap<>();
	private final static MinecraftClient minecraft = DataUtil.getMinecraft();

	private static World currentWorld;
	private static WorldKey currentWorldKey;
	private static BlockPos currentWorldPos;
	private static String currentWorldName;
	private static boolean cacheClearing = false;
	private static boolean requestWorldName = false;
	
	public static void onConfigUpdate() {
		if (currentWorld == null) return;
		JustMapClient.canMapping = false;
		if (!RuleUtil.detectMultiworlds()) {
			if (currentWorldPos != null || currentWorldName != null) {
				currentWorldPos = null;
				currentWorldName = null;
				close();
			}
		} else if (MultiworldDetection.isManual()) {
			if (currentWorldPos != null) {
				currentWorldPos = null;
				close();
			}
		} else if (MultiworldDetection.isAuto()) {
			if (currentWorldName != null) {
				currentWorldName = null;
				close();
			}
		}
		updateWorldKey();
		JustMapClient.canMapping = true;
	}
	
	public static void onWorldChanged(World world) {
		currentWorld = world;
		updateWorldKey();
		if (RuleUtil.detectMultiworlds()) {
			JustMap.LOGGER.debug("World changed, stop mapping!");
			JustMapClient.canMapping = false;
			if (MultiworldDetection.isManual()) {
				requestWorldName = true;
				setCurrentWorldName("Default");
			}
		} else {
			JustMapClient.canMapping = true;
		}
	}
	
	public static void onWorldPosChanged(BlockPos newPos) {
		if (!RuleUtil.detectMultiworlds() || MultiworldDetection.isManual()) {
			currentWorldPos = null;
			updateWorldKey();
			JustMapClient.canMapping = true;
			return;
		}
		if (currentWorldPos == null) {
			synchronized (WORLDS_DATA) {
				WORLDS_DATA.keySet().forEach(key -> {
					key.setWorldPos(newPos);
				});
			}
		}
		currentWorldPos = newPos;
		updateWorldKey();
		JustMapClient.canMapping = true;
	}
	
	public static void setCurrentWorldName(String name) {
		if (!RuleUtil.detectMultiworlds() || MultiworldDetection.isAuto()) {
			currentWorldName = null;
			updateWorldKey();
			JustMapClient.canMapping = true;
			requestWorldName = false;
			return;
		}
		if (currentWorldName == null) {
			synchronized (WORLDS_DATA) {
				WORLDS_DATA.keySet().forEach(key -> {
					if (name.equals(key.getName())) {
						key.setWorldName(name);
					}
				});
			}
		}
		currentWorldName = name;
		updateWorldKey();
		JustMapClient.canMapping = true;
		requestWorldName = false;
	}
	
	public static WorldKey getWorldKey() {
		return currentWorldKey;
	}
	
	public static WorldKey createWorldKey(World world, BlockPos blockPos, String worldName) {
		WorldKey newKey = new WorldKey(world.getRegistryKey());
		if (RuleUtil.detectMultiworlds()) {
			if (blockPos != null) {
				newKey.setWorldPos(blockPos);
			}
			if (worldName != null) {
				newKey.setWorldName(worldName);
			}
		}
		
		return newKey;
	}
	
	private static void updateWorldKey() {
		WorldKey newKey = createWorldKey(currentWorld, currentWorldPos, currentWorldName);
		if (!newKey.equals(currentWorldKey)) {
			currentWorldKey = newKey;
		}
	}

	public static WorldData getData() {
		return getData(currentWorld, currentWorldKey);
	}

	public static WorldData getData(World world, WorldKey worldKey) {
		if (world == null || worldKey == null) return null;
		
		WorldData data;
		synchronized (WORLDS_DATA) {
			if (WORLDS_DATA.containsKey(worldKey)) {
				return WORLDS_DATA.get(worldKey);
			} else {
				data = new WorldData(world);
				WORLDS_DATA.put(worldKey, data);
			}
		}
		
		return data;
	}
	
	public static void onChunkLoad(World world, WorldChunk worldChunk) {
		if (world == null || worldChunk == null || worldChunk.isEmpty()) return;
		IMap map = DataUtil.getMap();
		WorldData mapData = getData();
		ChunkData mapChunk = mapData.getChunk(worldChunk.getPos());
		ChunkUpdateEvent updateEvent = new ChunkUpdateEvent(worldChunk, mapChunk, map.getLayer(), map.getLevel(), true);
		ChunkUpdateListener.accept(updateEvent);
	}
	
	public static void update() {
		if (requestWorldName && minecraft.currentScreen == null) {
			
		}
		getData().updateMap();
		JustMap.WORKER.execute(() -> {
			synchronized (WORLDS_DATA) {
				WORLDS_DATA.forEach((id, data) -> {
					data.clearCache();
				});
			}
		});
	}

	public static void memoryControl() {
		if (cacheClearing) return;
		long usedPct = MemoryUtil.getMemoryUsage();
		if (usedPct >= 85L) {
			cacheClearing = true;
			JustMap.LOGGER.warning(String.format("Memory usage at %2d%%, forcing garbage collection.", usedPct));
			JustMap.WORKER.execute("Hard cache clearing...", () -> {
				int amount = ClientParams.purgeAmount * 10;
				synchronized (WORLDS_DATA) {
					WORLDS_DATA.forEach((id, world) -> {
						world.getChunkManager().purge(amount, 1000);
					});
				}
				System.gc();
				cacheClearing = false;
			});
			usedPct = MemoryUtil.getMemoryUsage();
			JustMap.LOGGER.warning(String.format("Memory usage at %2d%%.", usedPct));
		}
	}
	
	public static void close() {
		synchronized (WORLDS_DATA) {
			if (WORLDS_DATA.size() > 0) {
				WORLDS_DATA.forEach((id, data) -> data.close());
				WORLDS_DATA.clear();
			}
		}
	}
}
