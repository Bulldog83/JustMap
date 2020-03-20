package ru.bulldog.justmap.util;

import java.io.File;
import java.io.IOException;

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
	private static int currentDimId = 0;
	
	public final static File MAP_DIR = new File(minecraft.runDirectory, "justmap/");
	
	public final static TaskManager LOADER = new TaskManager("cache-loader");
	public final static TaskManager IO = new TaskManager("cache-io");

	public static synchronized CompoundTag getCache(File cacheFile) {
		CompoundTag chunkCache = null;			
		try {
			chunkCache = NbtIo.read(cacheFile);
		} catch(IOException ex) {}
		
		if (chunkCache == null) chunkCache = new CompoundTag();
		
		return chunkCache;
	}
	
	public static synchronized void saveCache(File cacheFile, CompoundTag data) {
		try {
			NbtIo.safeWrite(data, cacheFile);
		} catch (Exception ex) {
			JustMap.LOGGER.catching(ex);
		} finally {
			data = null;
		}
	}
	
	public static File chunkDir(ChunkPos chunkPos) {
		RegionPos regPos = new RegionPos(chunkPos);
		File regDir = new File(chunksCacheDir(), String.format("%s/", regPos.toString()));
		
		if (!regDir.exists()) {
			regDir.mkdirs();
		}
		
		return regDir;
	}
	
	public static File chunksCacheDir() {
		File cacheDir = new File(cacheDir(), "chunk-data/");
		
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		
		return cacheDir;
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
