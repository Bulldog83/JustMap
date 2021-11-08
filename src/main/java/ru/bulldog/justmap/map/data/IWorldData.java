package ru.bulldog.justmap.map.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import ru.bulldog.justmap.map.IMap;

public interface IWorldData {
    IChunkData getChunk(ChunkPos chunkPos);
    IRegionData getRegion(IMap map, BlockPos blockPos);
    IChunkData getChunk(int x, int z);
}
