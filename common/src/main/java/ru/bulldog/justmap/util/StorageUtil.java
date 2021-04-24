package ru.bulldog.justmap.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import old_files.justmap.JustMap;
import old_files.justmap.client.map.data.WorldKey;
import old_files.justmap.client.map.data.WorldManager;
import old_files.justmap.util.DataUtil;
import old_files.justmap.util.Dimension;
import org.apache.commons.io.FileUtils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public final class StorageUtil {
	
	private StorageUtil() {}
	
	private final static FabricLoader fabricLoader = FabricLoader.getInstance();
	private final static Path GAME_DIR = fabricLoader.getGameDir();
	private final static Path GAME_CONFIG_DIR = fabricLoader.getConfigDir();
	private final static Path MAP_DATA_DIR = GAME_DIR.resolve(JustMap.MOD_ID);
	private final static Path MAP_CONFIG_DIR = GAME_CONFIG_DIR.resolve(JustMap.MOD_ID);
	private final static Path MAP_SKINS_DIR = MAP_CONFIG_DIR.resolve("skins");
	private final static Path MAP_ICONS_DIR = MAP_CONFIG_DIR.resolve("icons");
	
	private static File filesDir = new File(MAP_DATA_DIR.toFile(), "undefined");
	
	public static File mapDir() {
		return MAP_DATA_DIR.toFile();
	}
	
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
	
	public static File cacheDir() {
		String dimension = "undefined";
		Level world = DataUtil.getWorld();
		if (world != null) {
			ResourceKey<Level> dimKey = world.dimension();
			dimension = dimKey.location().getPath();
		}

		WorldKey worldKey = WorldManager.getWorldKey();
		File cacheDir = new File(filesDir(), worldKey.toFolder());
		File oldCacheDir = new File(filesDir(), String.format("cache/%s", dimension));
		if (world != null) {
			int dimId = Dimension.getId(world);
			if (dimId != Integer.MIN_VALUE) {
				File oldDir = new File(filesDir(), String.format("cache/DIM%d", dimId));
				if (oldDir.exists()) {
					oldDir.renameTo(cacheDir);
				}				
			}
		}
		if (oldCacheDir.exists()) {
			oldCacheDir.renameTo(cacheDir);
		} else if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		
		return cacheDir;
	}
	
	@Environment(EnvType.CLIENT)
	public static File filesDir() {
		Minecraft minecraft = Minecraft.getInstance();		
		ServerData serverInfo = minecraft.getCurrentServer();
		File dataDir = MAP_DATA_DIR.toFile();
		File mapsDir = new File(MAP_DATA_DIR.toFile(), "maps");
		if (minecraft.hasSingleplayerServer()) {
			MinecraftServer server = minecraft.getSingleplayerServer();
			String name = scrubFileName(server.getWorldData().getLevelName());
			filesDir = new File(mapsDir, "local/" + name);
			File oldDir = new File(dataDir, "local/" + name);
			if (oldDir.exists()) {
				try {
					FileUtils.moveDirectory(oldDir, filesDir);
				} catch (IOException ex) {
					JustMap.LOGGER.warning("Can't move directory!", oldDir, ex);
				}
			}
		} else if (serverInfo != null) {
			String name = scrubFileName(serverInfo.name);
			String address = serverInfo.ip;
			if (address.contains(":")) {
				int end = address.indexOf(":") - 1;
				address = address.substring(0, end);
			}
			filesDir = new File(mapsDir, String.format("servers/%s_(%s)", name, address));
			File oldDir = new File(dataDir, String.format("servers/%s_(%s)", name, address));
			if (oldDir.exists()) {
				try {
					FileUtils.moveDirectory(oldDir, filesDir);
				} catch (IOException ex) {
					JustMap.LOGGER.warning("Can't move directory!", oldDir, ex);
				}
			}
		}
		
		if (!filesDir.exists()) {
			filesDir.mkdirs();
		}
		
		return filesDir;
	}

	private static String scrubFileName(String input) {
		input = input.replaceAll("[ /\\\\]+", "_");
		input = input.replaceAll("[,:&\"|<>?*]", "_");

		return input;
	}
}