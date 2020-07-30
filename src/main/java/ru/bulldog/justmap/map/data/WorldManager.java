package ru.bulldog.justmap.map.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.screen.WorldnameScreen;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.event.ChunkUpdateEvent;
import ru.bulldog.justmap.event.ChunkUpdateListener;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.tasks.MemoryUtil;

public final class WorldManager {

	private final static Map<WorldKey, WorldData> worldsData = new HashMap<>();
	private final static MinecraftClient minecraft = DataUtil.getMinecraft();
	private final static Map<BlockPos, String> registeredWorlds = new HashMap<>();
	private final static Set<String> registeredNames = new HashSet<>();

	private static World currentWorld;
	private static WorldKey currentWorldKey;
	private static BlockPos currentWorldPos;
	private static String currentWorldName;
	private static boolean cacheClearing = false;
	private static boolean requestWorldName = false;
	
	public static List<String> getRegisteredNames() {
		return Lists.newArrayList(registeredNames);
	}
	
	public static String currentWorldName() {
		return currentWorldName != null ? currentWorldName : "Default";
	}
	
	public static void onConfigUpdate() {
		if (currentWorld == null) return;
		JustMapClient.stopMapping();
		if (!RuleUtil.detectMultiworlds()) {
			if (currentWorldPos != null || currentWorldName != null) {
				currentWorldPos = null;
				currentWorldName = null;
				updateWorldKey();
				close();
			}
		} else if (MultiworldDetection.isManual()) {
			if (currentWorldPos != null) {
				currentWorldPos = null;
				updateWorldKey();
				close();
			}
		} else if (MultiworldDetection.isAuto()) {
			if (currentWorldName != null) {
				currentWorldName = null;
				updateWorldKey();
				close();
			}
		}
		JustMapClient.startMapping();
	}
	
	public static void onWorldChanged(World world) {
		currentWorld = world;
		updateWorldKey();
		if (RuleUtil.detectMultiworlds()) {
			JustMap.LOGGER.debug("World changed, stop mapping!");
			JustMapClient.stopMapping();
			if (MultiworldDetection.isManual()) {
				requestWorldName = true;
			}
		} else {
			JustMapClient.startMapping();
		}
	}
	
	public static void onWorldPosChanged(BlockPos newPos) {
		if (!RuleUtil.detectMultiworlds()) {
			return;
		}
		if (MultiworldDetection.isManual()) {
			if (currentWorldPos != null) {
				currentWorldPos = null;
				updateWorldKey();
			}
			return;
		}
		JustMapClient.stopMapping();
		if (currentWorldPos == null) {
			synchronized (worldsData) {
				worldsData.keySet().forEach(key -> {
					key.setWorldPos(newPos);
				});
			}
		}
		currentWorldPos = newPos;
		if (!MultiworldDetection.isAuto()) {
			if (registeredWorlds.containsKey(newPos)) {
				currentWorldName = registeredWorlds.get(newPos);
				updateWorldKey();
				JustMapClient.startMapping();
			} else {
				requestWorldName = true;
			}
		} else {
			updateWorldKey();
			JustMapClient.startMapping();
		}
	}
	
	public static void setCurrentWorldName(String name) {
		if (!RuleUtil.detectMultiworlds()) {
			return;
		}
		if (currentWorldName == null) {
			synchronized (worldsData) {
				worldsData.keySet().forEach(key -> {
					if (name.equals(key.getName())) {
						key.setWorldName(name);
					}
				});
			}
		}
		registeredNames.add(name);
		currentWorldName = name;
		if (!MultiworldDetection.isManual()) {
			if (!registeredWorlds.containsKey(currentWorldPos)) {
				registeredWorlds.put(currentWorldPos, name);
			}
		}
		updateWorldKey();
		JustMapClient.startMapping();
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
		synchronized (worldsData) {
			if (worldsData.containsKey(worldKey)) {
				return worldsData.get(worldKey);
			} else {
				data = new WorldData(world);
				worldsData.put(worldKey, data);
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
		if (requestWorldName && !(minecraft.currentScreen instanceof ProgressScreen)) {
			minecraft.openScreen(new WorldnameScreen(minecraft.currentScreen));
			requestWorldName = false;
		}
		if (!JustMapClient.canMapping()) return;
		getData().updateMap();
		JustMap.WORKER.execute(() -> {
			synchronized (worldsData) {
				worldsData.forEach((id, data) -> {
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
				synchronized (worldsData) {
					worldsData.forEach((id, world) -> {
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
		synchronized (worldsData) {
			if (worldsData.size() > 0) {
				worldsData.forEach((id, data) -> data.close());
				worldsData.clear();
			}
		}
	}
}
