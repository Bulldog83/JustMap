package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.map.IMap;

public interface WorldMapper {
	MapRegion getMapRegion(IMap map, int blockX, int blockZ);

	int getMapHeight(Layer mapLayer, int mapLevel, int posX, int posZ);

	void onWorldMapperClose();
}
