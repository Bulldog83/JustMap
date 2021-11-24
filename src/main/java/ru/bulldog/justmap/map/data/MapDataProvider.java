package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.map.data.classic.WorldManager;
import ru.bulldog.justmap.map.multiworld.MultiworldManager;

public class MapDataProvider {
	public static MapDataManager getManager() {
		return WorldManager.WORLD_MANAGER;
	}
	public static MultiworldManager getMultiworldManager() {
		return MultiworldManager.MULTIWORLD_MANAGER;
	}
}
