package ru.bulldog.justmap.map.multiworld;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ProgressScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.screen.WorldnameScreen;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.map.data.WorldMapper;
import ru.bulldog.justmap.util.RuleUtil;

public final class MultiworldManager {
	public static final MultiworldManager MULTIWORLD_MANAGER = new MultiworldManager();
	private final MultiworldConfig config = new MultiworldConfig(this);

	// used only in mixed mode to associate world names with worlds
	private final Map<MultiworldIdentifier, String> multiworldNames = new HashMap<>();
	private final MinecraftClient minecraft = MinecraftClient.getInstance();

	private final Map<WorldKey, WorldMapper> worldMappers = new HashMap<>();

	private World currentWorld;
	private WorldKey currentWorldKey;
	private BlockPos currentWorldPos;
	private String currentWorldName;
	private boolean requestWorldName = false;
	private boolean isWorldLoaded = false;

	private void closeAllWorldMappers() {
		synchronized (worldMappers) {
			if (worldMappers.size() > 0) {
				worldMappers.forEach((id, data) -> {
					if (data != null) {
						data.onWorldMapperClose();
					}
				});
				worldMappers.clear();
			}
		}
	}

	public void onConfigUpdate() {
		if (currentWorld == null) return;
		config.saveConfig();

		JustMapClient.stopMapping();
		if (!RuleUtil.detectMultiworlds()) {
			if (currentWorldPos != null || currentWorldName != null) {
				currentWorldPos = null;
				currentWorldName = null;
				closeAllWorldMappers();
			}
			updateWorldKey();
			JustMapClient.startMapping();
			return;
		} else if (MultiworldDetection.isManual()) {
			if (currentWorldPos != null) {
				currentWorldPos = null;
				closeAllWorldMappers();
			}
			if (currentWorldName == null) {
				requestWorldName = true;
			} else {
				updateWorldKey();
				JustMapClient.startMapping();
			}
			return;
		} else if (MultiworldDetection.isAuto()) {
			if (currentWorldName != null) {
				currentWorldName = null;
				closeAllWorldMappers();
			}
			assert minecraft.world != null;
			onWorldSpawnPosChanged(minecraft.world.getSpawnPos());
			return;
		} else if (MultiworldDetection.isMixed()) {
			if (currentWorldPos == null) {
				assert minecraft.world != null;
				onWorldSpawnPosChanged(minecraft.world.getSpawnPos());
			} else if (currentWorldName == null) {
				requestWorldName = true;
			} else {
				updateWorldKey();
				JustMapClient.startMapping();
			}
			return;
		}
		JustMapClient.startMapping();
	}

	public void onWorldChanged(World world) {
		currentWorld = world;
		if (RuleUtil.detectMultiworlds()) {
			JustMap.LOGGER.debug("World changed, stop mapping!");
			JustMapClient.stopMapping();
			if (MultiworldDetection.isManual()) {
				requestWorldName = true;
			}
		} else {
			updateWorldKey();
			JustMapClient.startMapping();
		}
	}

	public void onWorldSpawnPosChanged(BlockPos newPos) {
		if (!RuleUtil.detectMultiworlds()) {
			return;
		}
		if (MultiworldDetection.isManual()) {
			if (currentWorldPos != null) {
				currentWorldPos = null;
				updateWorldKey();
			}
			return;
		}
		JustMapClient.stopMapping();
		currentWorldPos = newPos;
		if (MultiworldDetection.isMixed()) {
			MultiworldIdentifier identifier = new MultiworldIdentifier(newPos, currentWorld);
			String name = multiworldNames.get(identifier);
			if (name != null) {
				currentWorldName = name;
				updateWorldKey();
				JustMapClient.startMapping();
			} else {
				requestWorldName = true;
			}
		} else {
			updateWorldKey();
			JustMapClient.startMapping();
		}
	}

	public void storeMultiworldName(MultiworldIdentifier identifier, String name) {
		multiworldNames.put(identifier, name);
	}

	public Set<Map.Entry<MultiworldIdentifier, String>> getMultiworldNames() {
		return multiworldNames.entrySet();
	}

	public void setCurrentWorldName(String name) {
		if (name == "") {
			name = "Default";
		}

		if (!RuleUtil.detectMultiworlds()) {
			return;
		}
		currentWorldName = name;
		if (MultiworldDetection.isMixed()) {
			MultiworldIdentifier identifier = new MultiworldIdentifier(currentWorldPos, currentWorld);
			multiworldNames.put(identifier, name);
			config.saveWorldsConfig();
		}
		updateWorldKey();
		JustMapClient.startMapping();
	}

	public WorldKey getCurrentWorldKey() {
		return currentWorldKey;
	}

	public World getCurrentWorld() {
		return currentWorld;
	}

	public WorldKey createWorldKey(World world, BlockPos blockPos, String worldName) {
		WorldKey newKey = new WorldKey(world.getRegistryKey());
		if (RuleUtil.detectMultiworlds()) {
			if (blockPos != null) {
				newKey.setWorldPos(blockPos);
			}
			if (worldName != null) {
				newKey.setWorldName(worldName);
			}
		}

		return newKey;
	}

	public void updateWorldKey() {
		WorldKey newKey = createWorldKey(currentWorld, currentWorldPos, currentWorldName);
		if (!newKey.equals(currentWorldKey)) {
			currentWorldKey = newKey;
		}
	}

	public void forEachWorldMapper(BiConsumer<WorldKey, WorldMapper> consumer) {
		synchronized (worldMappers) {
			worldMappers.forEach((key, worldMapper) -> {
				consumer.accept(key, worldMapper);
			});
		}
	}

	// Only to be used by MapDataManagers
	public WorldMapper getOrCreateWorldMapper(Supplier<WorldMapper> worldMapperCreator) {
		if (getCurrentWorld() == null || getCurrentWorldKey() == null) return null;

		WorldMapper worldMapper;
		synchronized (worldMappers) {
			if (worldMappers.containsKey(getCurrentWorldKey())) {
				worldMapper = worldMappers.get(getCurrentWorldKey());
				if (worldMapper != null) {
					return worldMapper;
				}
				worldMapper = worldMapperCreator.get();
				worldMappers.replace(getCurrentWorldKey(), worldMapper);

				return worldMapper;
			}
			worldMapper = worldMapperCreator.get();
			worldMappers.put(getCurrentWorldKey(), worldMapper);
		}
		return worldMapper;
	}

	public void onServerConnect() {
		isWorldLoaded = true;
		config.loadConfig();
		config.loadWorldsConfig();
	}




	private void checkForNewWorld() {
		if (requestWorldName && !(minecraft.currentScreen instanceof ProgressScreen)) {
			minecraft.setScreen(new WorldnameScreen(minecraft.currentScreen));
			requestWorldName = false;
		}
	}

	public void onTick(boolean isServer) {
		if (!isServer) {
			checkForNewWorld();
		}
	}

	public void onWorldStop() {
		JustMapClient.stopMapping();
		JustMap.WORKER.execute("Stopping world ...", () -> {
			config.reloadConfig();
			currentWorld = null;
			if (isWorldLoaded) {
				config.saveWorldsConfig();
				multiworldNames.clear();
				closeAllWorldMappers();
				isWorldLoaded = false;
			}
		});
	}

}
