package ru.bulldog.justmap.map.multiworld;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.client.screen.WorldnameScreen;
import ru.bulldog.justmap.config.ConfigKeeper;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.map.data.WorldMapper;
import ru.bulldog.justmap.util.JsonFactory;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.storage.StorageUtil;

public final class MultiworldManager {
	public static final MultiworldManager MULTIWORLD_MANAGER = new MultiworldManager();

	// used only in mixed mode to associate world names with worlds
	private final Map<MultiworldIdentifier, String> worldAssociations = new HashMap<>();
	private final MinecraftClient minecraft = MinecraftClient.getInstance();
	private final ClientConfig modConfig = JustMapClient.getConfig();

	private final Map<WorldKey, WorldMapper> worldMappers = new HashMap<>();

	private World currentWorld;
	private WorldKey currentWorldKey;
	private BlockPos currentWorldPos;
	private String currentWorldName;
	private boolean requestWorldName = false;
	private boolean isWorldLoaded = false;

	private void closeAllWorldMappers() {
		synchronized (worldMappers) {
			if (worldMappers.size() > 0) {
				worldMappers.forEach((id, data) -> {
					if (data != null) {
						data.onWorldMapperClose();
					}
				});
				worldMappers.clear();
			}
		}
	}

	public void onConfigUpdate() {
		if (currentWorld == null) return;
		saveConfig();

		JustMapClient.stopMapping();
		if (!RuleUtil.detectMultiworlds()) {
			if (currentWorldPos != null || currentWorldName != null) {
				currentWorldPos = null;
				currentWorldName = null;
				closeAllWorldMappers();
			}
			updateWorldKey();
			JustMapClient.startMapping();
			return;
		} else if (MultiworldDetection.isManual()) {
			if (currentWorldPos != null) {
				currentWorldPos = null;
				closeAllWorldMappers();
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
				closeAllWorldMappers();
			}
			assert minecraft.world != null;
			onWorldSpawnPosChanged(minecraft.world.getSpawnPos());
			return;
		} else if (MultiworldDetection.isMixed()) {
			if (currentWorldPos == null) {
				assert minecraft.world != null;
				onWorldSpawnPosChanged(minecraft.world.getSpawnPos());
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

	public void onWorldChanged(World world) {
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

	public void onWorldSpawnPosChanged(BlockPos newPos) {
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

	public void setCurrentWorldName(String name) {
		if (name == "") {
			name = "Default";
		}

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

	public WorldKey getCurrentWorldKey() {
		return currentWorldKey;
	}

	public World getCurrentWorld() {
		return currentWorld;
	}

	public WorldKey createWorldKey(World world, BlockPos blockPos, String worldName) {
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

	public void updateWorldKey() {
		WorldKey newKey = createWorldKey(currentWorld, currentWorldPos, currentWorldName);
		if (!newKey.equals(currentWorldKey)) {
			currentWorldKey = newKey;
		}
	}

	public void forEachWorldMapper(BiConsumer<WorldKey, WorldMapper> consumer) {
		synchronized (worldMappers) {
			worldMappers.forEach((key, worldMapper) -> {
				consumer.accept(key, worldMapper);
			});
		}
	}

	// Only to be used by MapDataManagers
	public WorldMapper getOrCreateWorldMapper(Supplier<WorldMapper> worldMapperCreator) {
		if (getCurrentWorld() == null || getCurrentWorldKey() == null) return null;

		WorldMapper worldMapper;
		synchronized (worldMappers) {
			if (worldMappers.containsKey(getCurrentWorldKey())) {
				worldMapper = worldMappers.get(getCurrentWorldKey());
				if (worldMapper != null) {
					return worldMapper;
				}
				worldMapper = worldMapperCreator.get();
				worldMappers.replace(getCurrentWorldKey(), worldMapper);

				return worldMapper;
			}
			worldMapper = worldMapperCreator.get();
			worldMappers.put(getCurrentWorldKey(), worldMapper);
		}
		return worldMapper;
	}

	public void onServerConnect() {
		isWorldLoaded = true;
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

	public void saveWorlds() {
		JsonArray worldsArray = new JsonArray();
		for (Map.Entry<MultiworldIdentifier, String> entry : worldAssociations.entrySet()) {
			JsonObject object = entry.getKey().toJson();
			object.addProperty("name", entry.getValue());
			worldsArray.add(object);
		}
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("worlds", worldsArray);
		File worldsFile = new File(StorageUtil.filesDir(), "worlds.json");
		JsonFactory.storeJson(worldsFile, jsonObject);
	}

	public void saveConfig() {
		ConfigKeeper.EnumEntry<MultiworldDetection> detectionType = modConfig.getEntry("multiworld_detection");
		ConfigKeeper.BooleanEntry detectMultiworlds = modConfig.getEntry("detect_multiworlds");
		File configFile = new File(StorageUtil.filesDir(), "config.json");
		JsonObject configObject = new JsonObject();
		configObject.addProperty("detect_multiworlds", detectMultiworlds.asString());
		configObject.addProperty("multiworld_detection_type", detectionType.asString());
		JsonFactory.storeJson(configFile, configObject);
	}

	// try to load local configuration, return false if not successful
	public boolean tryLoadConfig() {
		File configFile = new File(StorageUtil.filesDir(), "config.json");
		if (!configFile.exists()) return false;
		try {
			JsonObject configObject = JsonFactory.getJsonObject(configFile);
			ConfigKeeper.EnumEntry<MultiworldDetection> detectionType = modConfig.getEntry("multiworld_detection");
			ConfigKeeper.BooleanEntry detectMultiworlds = modConfig.getEntry("detect_multiworlds");
			detectMultiworlds.fromString(JsonHelper.getString(configObject, "detect_multiworlds"));
			detectionType.fromString(JsonHelper.getString(configObject, "multiworld_detection_type"));
		} catch (JsonSyntaxException ex) {
			return false;
		}
		return true;
	}

	public void loadConfig() {
		if (!tryLoadConfig()) {
			// load defaults
			ConfigKeeper.EnumEntry<MultiworldDetection> detectionType = modConfig.getEntry("multiworld_detection");
			ConfigKeeper.BooleanEntry detectMultiworlds = modConfig.getEntry("detect_multiworlds");
			detectionType.setValue(detectionType.getDefault());
			detectMultiworlds.setValue(detectMultiworlds.getDefault());
			// then save them
			saveConfig();
		}
	}

	private void checkForNewWorld() {
		if (requestWorldName && !(minecraft.currentScreen instanceof ProgressScreen)) {
			minecraft.setScreen(new WorldnameScreen(minecraft.currentScreen));
			requestWorldName = false;
		}
	}

	public void onTick(boolean isServer) {
		if (!isServer) {
			checkForNewWorld();
		}
	}

	public void onWorldStop() {
		JustMapClient.stopMapping();
		JustMap.WORKER.execute("Stopping world ...", () -> {
			modConfig.reloadFromDisk();
			currentWorld = null;
			if (isWorldLoaded) {
				saveWorlds();
				worldAssociations.clear();
				closeAllWorldMappers();
				isWorldLoaded = false;
			}
		});
	}

}
