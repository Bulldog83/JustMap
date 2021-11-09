package ru.bulldog.justmap.map.data;

import net.minecraft.util.math.BlockPos;
import ru.bulldog.justmap.map.IMap;

public interface IWorldData {
    IRegionData getRegion(IMap map, BlockPos blockPos);
}
