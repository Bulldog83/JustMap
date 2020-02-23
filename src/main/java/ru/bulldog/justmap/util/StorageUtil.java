package ru.bulldog.justmap.util;

import java.io.File;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.MinecraftServer;

public class StorageUtil {	
	
	public final static File MAP_DIR = new File(MinecraftClient.getInstance().runDirectory, "justmap/");
	
	public static File filesDir() {
		MinecraftClient client = MinecraftClient.getInstance();
		
		File filesDir;
		if (client.isIntegratedServerRunning()) {
			MinecraftServer server = client.getServer();
			filesDir = new File(MAP_DIR, String.format("local/%s/", server.getLevelName()));
		} else if (!client.isInSingleplayer()) {
			ServerInfo server = client.getCurrentServerEntry();
			filesDir = new File(MAP_DIR, String.format("servers/%s/", server.name));
		} else {		
			filesDir = new File(MAP_DIR, "undefined/");
		}
		
		if (!filesDir.exists()) {
			filesDir.mkdirs();
		}
		
		return filesDir;
	}
	
	public static File cacheDir() {
		File cacheDir = new File(filesDir(), "cache/");
		
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		
		return cacheDir;
	}
}
