package ru.bulldog.justmap.util.storage;

import java.io.File;
import java.nio.file.Path;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.Dimension;

public final class StorageUtil {
	
	private StorageUtil() {}
	
	private final static FabricLoader fabricLoader = FabricLoader.getInstance();
	private final static Path GAME_DIR = fabricLoader.getGameDir();
	private final static Path GAME_CONFIG_DIR = fabricLoader.getConfigDir();
	private final static Path MAP_DATA_DIR = GAME_DIR.resolve(JustMap.MODID);
	private final static Path MAP_CONFIG_DIR = GAME_CONFIG_DIR.resolve(JustMap.MODID);
	private final static Path MAP_SKINS_DIR = MAP_CONFIG_DIR.resolve("skins");
	private final static Path MAP_ICONS_DIR = MAP_CONFIG_DIR.resolve("icons");
	
	private static File filesDir = new File(MAP_DATA_DIR.toFile(), "undefined");
	private static String currentDim = "unknown";
	
	public static File configDir() {
		File mapConfigDir = MAP_CONFIG_DIR.toFile();
		if (!mapConfigDir.exists()) {
			mapConfigDir.mkdirs();
		}
		return mapConfigDir;
	}
	
	@Environment(EnvType.CLIENT)
	public static File skinsDir() {
		File mapSkinsDir = MAP_SKINS_DIR.toFile();
		if (!mapSkinsDir.exists()) {
			mapSkinsDir.mkdirs();
		}
		return mapSkinsDir;
	}
	
	@Environment(EnvType.CLIENT)
	public static File iconsDir() {
		File iconsDir = MAP_ICONS_DIR.toFile();
		if (!iconsDir.exists()) {
			iconsDir.mkdirs();
		}
		return iconsDir;
	}
	
	@Environment(EnvType.CLIENT)
	public static File cacheDir() {
		RegistryKey<DimensionType> dimKey = null;
		World world = DataUtil.getWorld();
		if (world != null) {
			dimKey = world.getDimensionRegistryKey();			
			String dimension = dimKey.getValue().getPath();
			if (!currentDim.equals(dimension)) {
				currentDim = dimension;
			}			
		}

		File cacheDir = new File(filesDir(), String.format("cache/%s", currentDim));
		if (dimKey != null) {
			int dimId = Dimension.getId(dimKey);
			if (dimId != Integer.MIN_VALUE) {
				File oldDir = new File(filesDir(), String.format("cache/DIM%d", dimId));
				if (oldDir.exists()) {
					oldDir.renameTo(cacheDir);
				}				
			}
		}
		
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		
		return cacheDir;
	}
	
	@Environment(EnvType.CLIENT)
	public static File filesDir() {
		MinecraftClient minecraft = DataUtil.getMinecraft();		
		ServerInfo serverInfo = minecraft.getCurrentServerEntry();
		File mapDataDir = MAP_DATA_DIR.toFile();
		if (minecraft.isIntegratedServerRunning()) {
			MinecraftServer server = minecraft.getServer();
			String name = scrubNameFile(server.getSaveProperties().getLevelName());
			filesDir = new File(mapDataDir, String.format("local/%s", name));
		} else if (serverInfo != null) {
			String name = scrubNameFile(serverInfo.name);
			filesDir = new File(mapDataDir, String.format("servers/%s", name));
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

	private static String scrubNameFile(String input) {
		input = input.replace("<", "_");
		input = input.replace(">", "_");
		input = input.replace(":", "_");
		input = input.replace("\"", "_");
		input = input.replace("/", "_");
		input = input.replace("\\", "_");
		input = input.replace("//", "_");
		input = input.replace("|", "_");
		input = input.replace("?", "_");
		input = input.replace("*", "_");

		return input;
	}
}