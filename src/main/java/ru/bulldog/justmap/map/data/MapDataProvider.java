package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.map.data.classic.WorldManager;

public class MapDataProvider {
    private static IWorldManager worldManager = new WorldManager();

    public static IWorldManager getWorldManager() {
        return worldManager;
    }
}
