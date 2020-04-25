package ru.bulldog.justmap.util;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.VersionedChunkStorage;
import ru.bulldog.justmap.JustMap;

public class StorageUtil {	
	
	private static MinecraftClient minecraft = MinecraftClient.getInstance();
	
	public final static File MAP_DIR = new File(minecraft.runDirectory, "justmap/");
	public final static TaskManager IO = new TaskManager("cache-io");
	
	private static VersionedChunkStorage storage;
	private static File storageDir;
	private static File filesDir = new File(MAP_DIR, "undefined/");
	
	private static int currentDimId = 0;

	public static synchronized CompoundTag getCache(ChunkPos pos) {
		updateCacheStorage();
		try {
			CompoundTag data = storage.getNbt(pos);
			return data != null ? data : new CompoundTag();
		} catch (IOException ex) {}
		
		return new CompoundTag();
	}
	
	public static synchronized void saveCache(ChunkPos pos, CompoundTag data) {
		updateCacheStorage();
		storage.setTagAt(pos, data);
	}
	
	public static void updateCacheStorage() {
		File cacheDir = new File(cacheDir(), "chunk-data/");
		if (storageDir == null || !storageDir.equals(cacheDir)) {		
			storageDir = cacheDir;
			
			if (!storageDir.exists()) {
				storageDir.mkdirs();
			}
			
			if (storage != null) {
				storage.completeAll();
				
				try {
					storage.close();
				} catch (IOException ex) {
					JustMap.LOGGER.catching(ex);
				}
			}
			
			storage = new VersionedChunkStorage(storageDir, minecraft.getDataFixer());
		}
	}
	
	public static File cacheDir() {
		if (minecraft.world != null) {
			int dimension = minecraft.world.getDimension().getType().getRawId();
			currentDimId = currentDimId != dimension ? dimension : currentDimId;
		}
		
		File cacheDir = new File(filesDir(), String.format("cache/DIM%d/", currentDimId));
		
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		
		return cacheDir;
	}
	
	public static File filesDir() {
		MinecraftClient client = MinecraftClient.getInstance();
		
		ServerInfo serverInfo = client.getCurrentServerEntry();
		if (client.isIntegratedServerRunning()) {
			MinecraftServer server = client.getServer();
			filesDir = new File(MAP_DIR, String.format("local/%s/", server.getLevelName()));
		} else if (serverInfo != null) {
			filesDir = new File(MAP_DIR, String.format("servers/%s/", serverInfo.name));
		}
		
		if (!filesDir.exists()) {
			filesDir.mkdirs();
		}
		
		return filesDir;
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
