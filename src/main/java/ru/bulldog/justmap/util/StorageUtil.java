package ru.bulldog.justmap.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.data.RegionPos;

public class StorageUtil {	
	
	private static MinecraftClient minecraft = MinecraftClient.getInstance();
	
	public final static File MAP_DIR = new File(minecraft.runDirectory, "justmap/");
	public static final TaskManager CACHEIO = new TaskManager("cache-io");
	
	private static volatile Map<RegionPos, CompoundTag> mapCache;
	
	private static long lastSave = 0;
	
	public static synchronized CompoundTag getCache(ChunkPos pos) {
		if (mapCache == null) {
			mapCache = new HashMap<>();
		}
		
		RegionPos regPos = new RegionPos(pos);
		File cacheFile = new File(chunksCacheDir(), String.format("%s.dat", regPos.toString()));		
		
		if (!mapCache.containsKey(regPos)) {
			CompoundTag regCache = null;			
			try {
				regCache = NbtIo.read(cacheFile);
			} catch(IOException ex) {}
			
			if (regCache == null) regCache = new CompoundTag();
			mapCache.put(regPos, regCache);
			
			return regCache;
		}
		
		return mapCache.get(regPos);
	}
	
	public static void saveCache() {
		long time = System.currentTimeMillis();
		if (time - lastSave < 10000) return;
		
		JustMap.EXECUTOR.execute(() -> {
			StorageUtil.storeCache();			
			lastSave = time;
		});
	}
	
	public static synchronized void storeCache() {
		if (mapCache == null) return;
		
		JustMap.LOGGER.debug("Storing map data...");
		
		mapCache.forEach((regPos, tag) -> {
			File cacheFile = new File(chunksCacheDir(), String.format("%s.dat", regPos.toString()));
			try {
				NbtIo.safeWrite(tag, cacheFile);			
			} catch (Exception ex) {
				JustMap.LOGGER.catching(ex);
			}
		});			
		
		JustMap.LOGGER.debug("Map data successfully saved.");
	}
	
	public static File filesDir() {
		MinecraftClient client = MinecraftClient.getInstance();
		
		File filesDir;
		ServerInfo serverInfo = client.getCurrentServerEntry();
		if (client.isIntegratedServerRunning()) {
			MinecraftServer server = client.getServer();
			filesDir = new File(MAP_DIR, String.format("local/%s/", server.getLevelName()));
		} else if (serverInfo != null) {
			filesDir = new File(MAP_DIR, String.format("servers/%s/", serverInfo.name));
		} else {		
			filesDir = new File(MAP_DIR, "undefined/");
		}
		
		if (!filesDir.exists()) {
			filesDir.mkdirs();
		}
		
		return filesDir;
	}
	
	public static File cacheDir() {
		int dimension = minecraft.world.getDimension().getType().getRawId();		
		File cacheDir = new File(filesDir(), String.format("cache/DIM%d/", dimension));
		
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		
		return cacheDir;
	}
	
	public static File chunksCacheDir() {
		File cacheDir = new File(cacheDir(), "chunk-data/");
		
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		
		return cacheDir;
	}
	
	public static void clearCache(File dir) {
		deleteDir(dir);
		dir.mkdirs();
	}
	
	public static void clearCache() {
		clearCache(cacheDir());
	}
	
	private static void deleteDir(File dir) {
		if (!dir.exists()) return;
		
		File[] files = dir.listFiles();
		if (files == null) {
			dir.delete();
			return;
		}
		
		for (File file : files) {
			if (file.isDirectory()) {
				deleteDir(file);
			} else {
				file.delete();
			}
		}
		dir.delete();
	}
}
