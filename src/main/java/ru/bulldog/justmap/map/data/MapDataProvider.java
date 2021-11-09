package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.map.data.classic.WorldManager;
import ru.bulldog.justmap.map.multiworld.MultiworldManager;
import ru.bulldog.justmap.map.data.fast.FastMapManager;

public class MapDataProvider {
	public final static boolean useFastMapData = true;
	public static MapDataManager getManager() {
		if (useFastMapData) {
			return FastMapManager.MANAGER;
		} else {
			return WorldManager.WORLD_MANAGER;
		}
	}
	public static MultiworldManager getMultiworldManager() {
		return MultiworldManager.MULTIWORLD_MANAGER;
	}
}
