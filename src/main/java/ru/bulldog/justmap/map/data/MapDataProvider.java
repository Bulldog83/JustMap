package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.map.data.classic.WorldManager;

public class MapDataProvider {
    public static IWorldManager getWorldManager() {
        return WorldManager.WORLD_MANAGER;
    }
}
