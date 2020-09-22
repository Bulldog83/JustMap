package ru.bulldog.justmap.map.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.client.config.ClientSettings;
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
	// used only in mixed mode to associate world names with worlds
	private final static Map<MultiworldIdentifier, String> worldAssociations = new HashMap<>();
	private final static MinecraftClient minecraft = MinecraftClient.getInstance();
	private final static ClientConfig modConfig = JustMapClient.getConfig();

	private static World currentWorld;
	private static WorldKey currentWorldKey;
	private static BlockPos currentWorldPos;
	private static String currentWorldName;
	private static boolean cacheClearing = false;
	private static boolean requestWorldName = false;
	private static boolean loaded = false;
	
	public static List<WorldKey> registeredWorlds() {
		return new ArrayList<>(worldsData.keySet());
	}
	
	public static String currentWorldName() {
		return currentWorldName != null ? currentWorldName : "Default";
	}
	
	public static void onConfigUpdate() {
		if (currentWorld == null) return;
		saveConfig();
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
			assert minecraft.world != null;
			onWorldPosChanged(minecraft.world.getSpawnPos());
			return;
		} else if (MultiworldDetection.isMixed()) {
			if (currentWorldPos == null) {
				assert minecraft.world != null;
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
		currentWorldPos = newPos;
		if (MultiworldDetection.isMixed()) {
			MultiworldIdentifier identifier = new MultiworldIdentifier(newPos, currentWorld);
			String name = worldAssociations.get(identifier);
			if (name != null) {
				currentWorldName = name;
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
		currentWorldName = name;
		if (MultiworldDetection.isMixed()) {
			MultiworldIdentifier identifier = new MultiworldIdentifier(currentWorldPos, currentWorld);
			worldAssociations.put(identifier, name);
			saveWorlds();
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
		ChunkUpdateEvent updateEvent = new ChunkUpdateEvent(worldChunk, mapChunk, map.getLayer(), map.getLevel(), 0, 0, 16, 16, true);
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
				int amount = ClientSettings.purgeAmount * 10;
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
		loaded = true;
		loadConfig();
		File worldsFile = new File(StorageUtil.filesDir(), "worlds.json");
		if (!worldsFile.exists()) return;
		JsonObject jsonObject = JsonFactory.getJsonObject(worldsFile);
		if (jsonObject.has("worlds") && jsonObject.get("worlds").isJsonArray()) {
			JsonArray worldsArray = jsonObject.getAsJsonArray("worlds");
			for (JsonElement elem : worldsArray) {
				if (!elem.isJsonObject()) continue;
				JsonObject object = (JsonObject) elem;
				MultiworldIdentifier identifier;
				try {
					identifier = MultiworldIdentifier.fromJson(object);
				} catch (JsonSyntaxException ex) {
					continue;
				}
				JsonElement nameElement = object.get("name");
				if (nameElement == null) continue;
				String name;
				try {
					name = nameElement.getAsString();
				} catch (ClassCastException ex) {
					continue;
				}
				worldAssociations.put(identifier, name);
			}
		}
	}
	
	private static void saveWorlds() {
		JsonArray worldsArray = new JsonArray();
		for (Entry<MultiworldIdentifier, String> entry : worldAssociations.entrySet()) {
			JsonObject object = entry.getKey().toJson();
			object.addProperty("name", entry.getValue());
			worldsArray.add(object);
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

	// try to load local configuration, return false if not successful
	private static boolean tryLoadConfig() {
		File configFile = new File(StorageUtil.filesDir(), "config.json");
		if (!configFile.exists()) return false;
		try {
			JsonObject configObject = JsonFactory.getJsonObject(configFile);
			EnumEntry<MultiworldDetection> detectionType = modConfig.getEntry("multiworld_detection");
			BooleanEntry detectMultiworlds = modConfig.getEntry("detect_multiworlds");
			detectMultiworlds.fromString(JsonHelper.getString(configObject, "detect_multiworlds"));
			detectionType.fromString(JsonHelper.getString(configObject, "multiworld_detection_type"));
		} catch (JsonSyntaxException ex) {
			return false;
		}
		return true;
	}
	
	private static void loadConfig() {
		if (!tryLoadConfig()) {
			// load defaults
			EnumEntry<MultiworldDetection> detectionType = modConfig.getEntry("multiworld_detection");
			BooleanEntry detectMultiworlds = modConfig.getEntry("detect_multiworlds");
			detectionType.setValue(detectionType.getDefault());
			detectMultiworlds.setValue(detectMultiworlds.getDefault());
			// then save them
			saveConfig();
		}
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
		if (loaded) {
			saveWorlds();
			worldAssociations.clear();
			clearData();
			loaded = false;
		}
	}
}
