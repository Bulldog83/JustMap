package ru.bulldog.justmap.util;

import java.io.File;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;

import ru.bulldog.justmap.map.data.ChunkStorage;

public class StorageUtil {
	
	private static MinecraftClient minecraft = MinecraftClient.getInstance();	
	public final static File MAP_DIR = new File(minecraft.runDirectory, "justmap/");
	
	private static ChunkStorage storage;
	private static File storageDir;
	private static File filesDir = new File(MAP_DIR, "undefined/");
	
	private static int currentDimId = 0;
	
	public static synchronized CompoundTag getCache(ChunkPos pos) {
		if (storage == null) updateCacheStorage();
		
		try {
			CompoundTag data = storage.getNbt(storageDir, pos);
			return data != null ? data : new CompoundTag();
		} catch (Exception ex) {
			return new CompoundTag();
		}		
	}
	
	public static synchronized void saveCache(ChunkPos pos, CompoundTag data) {
		if (storage == null) updateCacheStorage();
		storage.setTagAt(storageDir, pos, data);
	}
	
	public static void updateCacheStorage() {
		storageDir = new File(cacheDir(), "chunk-data/");

		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}		
		
		if (storage == null) storage = new ChunkStorage();
	}
	
	public static File cacheDir() {
		if (minecraft.world != null) {
			int dimension = minecraft.world.getDimension().getType().getRawId();
			if (currentDimId != dimension) {
				currentDimId = dimension;
			}
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
			filesDir = new File(MAP_DIR, String.format("local/%s/", server.method_27728().getLevelName()));
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
