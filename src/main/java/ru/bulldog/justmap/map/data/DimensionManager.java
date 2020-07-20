package ru.bulldog.justmap.map.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.tasks.MemoryUtil;

public final class DimensionManager {

	private final static MinecraftClient MINECRAFT = JustMapClient.MINECRAFT;
	private final static Map<Identifier, DimensionData> DIMENSION_DATA = new ConcurrentHashMap<>();
	private static Identifier currentDimension;
	private static World currentWorld;
	private static boolean cacheClearing = false;

	public static DimensionData getData(IMap map) {
		World world = MINECRAFT.world;
		if (world != null) {
			if (MINECRAFT.isIntegratedServerRunning()) {
				World serverWorld = MINECRAFT.getServer().getWorld(world.getRegistryKey());
				if (serverWorld != null) {
					world = serverWorld;
				}
			}
		}
		if (world == null) return null;
		if (!world.equals(currentWorld)) {
			currentWorld = world;
		}		
		Identifier dimId = currentWorld.getDimensionRegistryKey().getValue();
		if (!dimId.equals(currentDimension)) {
			currentDimension = dimId;
		}
		
		return getData(map, currentWorld, currentDimension);
	}

	public static DimensionData getData(IMap map, World world, Identifier dimension) {
		if (world == null) return null;
		
		DimensionData data;
		if (DIMENSION_DATA.containsKey(dimension)) {
			data = DIMENSION_DATA.get(dimension);
			if (!data.getWorld().equals(world)) {
				data.updateWorld(world);
				data.clear();
			} else {
				data.clearCache();
			}
			data.setLayer(map.getLayer(), map.getLevel());
		} else {
			data = new DimensionData(world, map.getLayer(), map.getLevel());
			DIMENSION_DATA.put(dimension, data);
		}
		
		return data;
	}

	public static void memoryControl() {
		if (cacheClearing) return;
		long usedPct = MemoryUtil.getMemoryUsage();
		if (usedPct >= 85L) {
			cacheClearing = true;
			JustMap.LOGGER.logWarning(String.format("Memory usage at %2d%%, forcing garbage collection.", usedPct));
			JustMap.WORKER.execute("Hard cache clearing...", () -> {
				int amount = ClientParams.purgeAmount * 10;
				DIMENSION_DATA.forEach((id, data) -> {
					data.getChunkManager().purge(amount, 1000);
				});
				System.gc();
				cacheClearing = false;
			});
			usedPct = MemoryUtil.getMemoryUsage();
			JustMap.LOGGER.logWarning(String.format("Memory usage at %2d%%.", usedPct));
		}
	}

	public static void clearData() {
		if (DIMENSION_DATA.size() > 0) {
			DIMENSION_DATA.forEach((id, data) -> data.clear());
			DIMENSION_DATA.clear();
		}
	}
}
