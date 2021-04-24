package old_files.justmap.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.server.packs.resources.Resource;
import old_files.justmap.JustMap;

import javax.annotation.Nullable;

public class JsonFactory {
	
	public final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	
	public static JsonObject getJsonObject(String path) throws IOException {
		try (InputStream is = JsonFactory.class.getResourceAsStream(path)) {
			Reader reader = new InputStreamReader(is);
			JsonObject jsonObject = loadJson(reader).getAsJsonObject();
			if (jsonObject == null) {
				return new JsonObject();
			}
			return jsonObject;
		}
	}
	
	public static JsonObject getJsonObject(Resource jsonSource) {
		if (jsonSource != null) {
			try (InputStream is = jsonSource.getInputStream()) {
				Reader reader = new InputStreamReader(is);
				JsonObject jsonObject = loadJson(reader).getAsJsonObject();
				if (jsonObject == null) {
					return new JsonObject();
				}
				return jsonObject;
			} catch (IOException ex) {
				JustMap.LOGGER.catching(ex);
				return new JsonObject();
			}
		}
		
		return new JsonObject();
	}
	
	public static JsonObject getJsonObject(File jsonFile) {
		if (jsonFile.exists()) {
			JsonElement jsonElem = loadJson(jsonFile);
			if (jsonElem == null) {
				return new JsonObject();
			}
			return jsonElem.getAsJsonObject();
		}
		
		return new JsonObject();
	}
	
	@Nullable
	public static JsonElement loadJson(File jsonFile) {
		if (jsonFile.exists()) {
			try (Reader reader = new FileReader(jsonFile)) {
				return loadJson(reader);
			} catch (Exception ex) {
				JustMap.LOGGER.catching(ex);
			}
		}
		return null;
	}
	
	public static JsonElement loadJson(Reader reader) {
		return GSON.fromJson(reader, JsonElement.class);
	}
	
	public static void storeJson(File jsonFile, JsonElement jsonObject) {
		try(FileWriter writer = new FileWriter(jsonFile)) {			
			String json = GSON.toJson(jsonObject);
			writer.write(json);
			writer.flush();
		} catch (IOException ex) {
			JustMap.LOGGER.catching(ex);
		}
	}
}
