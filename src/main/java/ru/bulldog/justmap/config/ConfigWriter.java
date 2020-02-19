package ru.bulldog.justmap.config;

import java.io.File;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.JsonFactory;

public class ConfigWriter extends JsonFactory {
	
	private final static File CONFIG_DIR = FabricLoader.getInstance().getConfigDirectory();
	private final static File CONFIG_FILE = new File(CONFIG_DIR, "/" + JustMap.MODID + ".json");
	
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
