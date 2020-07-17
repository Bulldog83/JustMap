package ru.bulldog.justmap.config;

import java.io.File;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.JsonFactory;
import ru.bulldog.justmap.util.storage.StorageUtil;

public class ConfigWriter extends JsonFactory {
	
	private final static File CONFIG_FILE = new File(StorageUtil.configDir(), JustMap.MODID + ".json");
	
	private static JsonObject configObject;
	
	private ConfigWriter() {}
	
	public static JsonObject load() {
		if (configObject == null) {
			configObject = loadJson(CONFIG_FILE);
		}
		
		return configObject;
	}
	
	public static void save(JsonElement config) {
		storeJson(CONFIG_FILE, config);
	}
}
