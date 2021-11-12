package ru.bulldog.justmap.map.multiworld;

import java.io.File;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JsonHelper;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.config.ConfigKeeper;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.util.JsonFactory;
import ru.bulldog.justmap.util.storage.StorageUtil;

public class MultiworldConfig {
	private final MultiworldManager manager;

	private final ClientConfig modConfig = JustMapClient.getConfig();

	public MultiworldConfig(MultiworldManager manager) {
		this.manager = manager;
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

	public void reloadConfig() {
		modConfig.reloadFromDisk();
	}

	public void loadWorldsConfig() {
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
				manager.storeMultiworldName(identifier, name);
			}
		}
	}

	public void saveWorldsConfig() {
		JsonArray worldsArray = new JsonArray();
		for (Map.Entry<MultiworldIdentifier, String> entry : manager.getMultiworldNames()) {
			JsonObject object = entry.getKey().toJson();
			object.addProperty("name", entry.getValue());
			worldsArray.add(object);
		}
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("worlds", worldsArray);
		File worldsFile = new File(StorageUtil.filesDir(), "worlds.json");
		JsonFactory.storeJson(worldsFile, jsonObject);
	}
}
