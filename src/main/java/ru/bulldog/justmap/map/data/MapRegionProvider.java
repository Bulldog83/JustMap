package ru.bulldog.justmap.map.data;

import net.minecraft.util.math.BlockPos;
import ru.bulldog.justmap.map.IMap;

public interface MapRegionProvider {
    MapRegion getMapRegion(IMap map, BlockPos blockPos);
}
