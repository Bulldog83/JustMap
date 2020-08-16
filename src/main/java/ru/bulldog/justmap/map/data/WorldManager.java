package ru.bulldog.justmap.map.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.screen.WorldnameScreen;
import ru.bulldog.justmap.config.ConfigKeeper.BooleanEntry;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.event.ChunkUpdateEvent;
import ru.bulldog.justmap.event.ChunkUpdateListener;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.JsonFactory;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.storage.StorageUtil;
import ru.bulldog.justmap.util.tasks.MemoryUtil;

public final class WorldManager {

	private final static Map<WorldKey, WorldData> worldsData = new HashMap<>();
	private final static Map<BlockPos, String> registeredWorlds = new HashMap<>();
	private final static MinecraftClient minecraft = DataUtil.getMinecraft();
	private final static ClientConfig modConfig = JustMapClient.CONFIG;

	private static World currentWorld;
	private static WorldKey currentWorldKey;
	private static BlockPos currentWorldPos;
	private static String currentWorldName;
	private static boolean cacheClearing = false;
	private static boolean requestWorldName = false;
	
	public static List<WorldKey> registeredWorlds() {
		return new ArrayList<>(worldsData.keySet());
	}
	
	public static String currentWorldName() {
		return currentWorldName != null ? currentWorldName : "Default";
	}
	
	public static void onConfigUpdate() {
		if (currentWorld == null) return;
		if (DataUtil.isOnline()) saveConfig();
		JustMapClient.stopMapping();
		if (!RuleUtil.detectMultiworlds()) {
			if (currentWorldPos != null || currentWorldName != null) {
				currentWorldPos = null;
				currentWorldName = null;
				clearData();
			}
			updateWorldKey();
			JustMapClient.startMapping();
			return;
		} else if (MultiworldDetection.isManual()) {
			if (currentWorldPos != null) {
				currentWorldPos = null;
				clearData();
			}
			if (currentWorldName == null) {
				requestWorldName = true;
			} else {
				updateWorldKey();
				JustMapClient.startMapping();
			}
			return;
		} else if (MultiworldDetection.isAuto()) {
			if (currentWorldName != null) {
				currentWorldName = null;
				clearData();
			}
			onWorldPosChanged(minecraft.world.getSpawnPos());
			return;
		} else if (MultiworldDetection.isMixed()) {
			if (currentWorldPos == null) {
				onWorldPosChanged(minecraft.world.getSpawnPos());
			} else if (currentWorldName == null) {
				requestWorldName = true;
			} else {
				updateWorldKey();
				JustMapClient.startMapping();
			}
			return;
		}
		JustMapClient.startMapping();
	}
	
	public static void onWorldChanged(World world) {
		currentWorld = world;
		if (RuleUtil.detectMultiworlds()) {
			JustMap.LOGGER.debug("World changed, stop mapping!");
			JustMapClient.stopMapping();
			if (MultiworldDetection.isManual()) {
				requestWorldName = true;
			}
		} else {
			updateWorldKey();
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
		if (MultiworldDetection.isMixed()) {
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
		currentWorldName = name;
		if (MultiworldDetection.isMixed()) {
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
				data = worldsData.get(worldKey);
				if (data != null) {
					return data;
				}
				data = new WorldData(world);
				worldsData.replace(worldKey, data);
				
				return data;
			}
			data = new WorldData(world);
			worldsData.put(worldKey, data);
		}
		return data;
	}
	
	public static void onChunkLoad(World world, WorldChunk worldChunk) {
		if (world == null || worldChunk == null || worldChunk.isEmpty()) return;
		IMap map = DataUtil.getMap();
		WorldData mapData = getData();
		if (mapData == null) return;
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
					if (data != null) data.clearCache();
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
						if (world != null) {
							world.getChunkManager().purge(amount, 1000);
						}
					});
				}
				System.gc();
				cacheClearing = false;
			});
			usedPct = MemoryUtil.getMemoryUsage();
			JustMap.LOGGER.warning(String.format("Memory usage at %2d%%.", usedPct));
		}
	}
	
	public static void load() {
		File worldsFile = new File(StorageUtil.filesDir(), "worlds.json");
		if (!worldsFile.exists()) return;
		JsonObject jsonObject = JsonFactory.loadJson(worldsFile);
		if (jsonObject.has("worlds")) {
			JsonArray worldsArray = jsonObject.getAsJsonArray("worlds");
			synchronized (worldsData) {
				for(JsonElement elem : worldsArray) {
					WorldKey world = WorldKey.fromJson((JsonObject) elem);
					worldsData.put(world, null);
				}
			}
		}
		loadConfig();
	}
	
	private static void saveWorlds() {
		if (worldsData.size() == 0) return;
		JsonArray worldsArray = new JsonArray();
		synchronized (worldsData) {
			worldsData.keySet().forEach(world -> {
				worldsArray.add(world.toJson());
			});
		}
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("worlds", worldsArray);
		File worldsFile = new File(StorageUtil.filesDir(), "worlds.json");
		JsonFactory.storeJson(worldsFile, jsonObject);
	}
	
	private static void saveConfig() {
		EnumEntry<MultiworldDetection> detectionType = modConfig.getEntry("multiworld_detection");
		BooleanEntry detectMultiworlds = modConfig.getEntry("detect_multiworlds");
		File configFile = new File(StorageUtil.filesDir(), "config.json");
		JsonObject configObject = new JsonObject();
		configObject.addProperty("detect_multiworlds", detectMultiworlds.asString());
		configObject.addProperty("multiworld_detection_type", detectionType.asString());
		JsonFactory.storeJson(configFile, configObject);
	}
	
	private static void loadConfig() {
		File configFile = new File(StorageUtil.filesDir(), "config.json");
		if (!configFile.exists()) return;
		JsonObject configObject = JsonFactory.loadJson(configFile);
		EnumEntry<MultiworldDetection> detectionType = modConfig.getEntry("multiworld_detection");
		BooleanEntry detectMultiworlds = modConfig.getEntry("detect_multiworlds");
		detectMultiworlds.fromString(JsonHelper.getString(configObject, "detect_multiworlds"));
		detectionType.fromString(JsonHelper.getString(configObject, "multiworld_detection_type"));
	}
	
	private static void clearData() {
		synchronized (worldsData) {
			if (worldsData.size() > 0) {
				worldsData.forEach((id, data) -> {
					if (data != null) data.close();
				});
				worldsData.clear();
			}
		}
	}
	
	public static void close() {
		modConfig.reloadFromDisk();
		currentWorld = null;
		saveWorlds();
		clearData();
	}
}
