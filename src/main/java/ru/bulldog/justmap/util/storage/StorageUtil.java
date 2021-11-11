package ru.bulldog.justmap.util.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.commons.io.FileUtils;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.data.MapDataProvider;
import ru.bulldog.justmap.map.data.WorldKey;
import ru.bulldog.justmap.mixins.SessionAccessor;
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

	public static File mapDir() {
		return MAP_DATA_DIR.toFile();
	}

	public static VersionedChunkStorage getChunkStorage(ServerWorld world) {
		File regionDir = new File(savesDir(world), "region");
		return new VersionedChunkStorage(regionDir, world.getServer().getDataFixer(), true);
	}

	public static File savesDir(ServerWorld world) {
		if (!(world instanceof ServerWorld)) return null;
		return ((SessionAccessor) world.getServer()).getServerSession().getWorldDirectory(world.getRegistryKey());
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
		World world = DataUtil.getWorld();
		if (world != null) {
			RegistryKey<World> dimKey = world.getRegistryKey();
			dimension = dimKey.getValue().getPath();
		}

		WorldKey worldKey = MapDataProvider.getManager().getWorldKey();
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
		MinecraftClient minecraft = MinecraftClient.getInstance();
		ServerInfo serverInfo = minecraft.getCurrentServerEntry();
		File dataDir = MAP_DATA_DIR.toFile();
		File mapsDir = new File(MAP_DATA_DIR.toFile(), "maps");
		if (minecraft.isIntegratedServerRunning()) {
			MinecraftServer server = minecraft.getServer();
			String name = scrubFileName(server.getSaveProperties().getLevelName());
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
			String address = serverInfo.address;
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
		input = input.replaceAll("[/\\ ]+", "_");
		input = input.replaceAll("[,:&\"\\|\\<\\>\\?\\*]", "_");

		return input;
	}
}
