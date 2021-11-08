package ru.bulldog.justmap.map.data.fast;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.IWorldData;

public class WorldData implements IWorldData {
    @Override
    public ChunkData getChunk(ChunkPos chunkPos) {
        return null;
    }

    @Override
    public ChunkData getChunk(int x, int z) {
        return null;
    }

    @Override
    public RegionData getRegion(IMap map, BlockPos blockPos) {
        return null;
    }
}
