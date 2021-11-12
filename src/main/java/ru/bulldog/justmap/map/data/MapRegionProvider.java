package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.map.IMap;

public interface MapRegionProvider {
	MapRegion getMapRegion(IMap map, int blockX, int blockZ);
	void onMultiworldClose();
}
