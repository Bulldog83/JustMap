package ru.bulldog.justmap.map.data.fast;

import ru.bulldog.justmap.map.data.WorldKey;

public class FastMapWorld {
    private final MapRegionRegistry registry = new MapRegionRegistry();

    public WorldKey getWorldKey() {
        return null;
    }

    public void setWorldName(String name) {
        // FIXME: should not be a no-op
    }

    public MapRegionRegistry getMapRegionProvider() {
        return registry;
    }
}
